/*
 * Copyright 2009-2016, Niklas Kyster Rasmussen, Flaming Candle
 *
 * This file is part of XML Creator for jEveAssets
 *
 * XML Creator for jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * XML Creator for jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XML Creator for jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package net.nikr.eve.io.creator.impl.yaml;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.inv.Blueprint;
import net.nikr.eve.io.data.inv.BlueprintActivity;
import net.nikr.eve.io.data.inv.BlueprintMaterial;
import net.nikr.eve.io.data.inv.Category;
import net.nikr.eve.io.data.inv.Group;
import net.nikr.eve.io.data.inv.MetaGroup;
import net.nikr.eve.io.data.inv.MetaType;
import net.nikr.eve.io.data.inv.Type;
import net.nikr.eve.io.data.inv.TypeAttribute;
import net.nikr.eve.io.data.inv.TypeMaterial;
import net.nikr.eve.io.online.EveCentralTest;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.InvReader;
import net.nikr.eve.util.Duration;
import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.api.UniverseApi;
import net.troja.eve.esi.model.UniverseNamesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ItemsYaml extends AbstractXmlWriter implements Creator{

	private final static Logger LOG = LoggerFactory.getLogger(ItemsYaml.class);

	@Override
	public boolean create() {
		Duration duration = new Duration();
		duration.start();
		LOG.info("Items:");
		try {
			LOG.info("	XML: init...");
			Document xmldoc = getXmlDocument("rows");
			Comment comment = xmldoc.createComment("Generated from Eve Online Toolkit. ©CCP hf. All rights reserved. Used with permission.");
			xmldoc.getDocumentElement().appendChild(comment);
			boolean success = createItems(xmldoc);
			LOG.info("	XML: Saving...");
			writeXmlFile(xmldoc, Program.getFilename(getFilename()));
			duration.end();
			LOG.info("	Items completed in " + duration.getString());
			return success;
		} catch (XmlException ex) {
			LOG.error("Items not saved (XML): "+ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	public String getFilename() {
		return "yaml"+File.separator+"items.xml";
	}

	@Override
	public String getName() {
		return "Items (YAML)";
	}

	private boolean createItems(Document xmldoc) {
		NumberFormat intFormat = new DecimalFormat("0");
		try {
			Set<String> blacklistedItems = new HashSet<>();
			Set<String> spacedItems = new HashSet<>();
			LOG.info("	YAML: Loading...");
			InvReader reader = new InvReader();
			LOG.info("		Types...");
			Map<Integer, Type> typeIDs = reader.loadTypes();
			LOG.info("		Groups...");
			Map<Integer, Group> groupIDs = reader.loadGroups();
			LOG.info("		Categories...");
			Map<Integer, Category> categories = reader.loadCategories();
			LOG.info("		Attributes...");
			Map<Integer, TypeAttribute> metaLevelAttributes = reader.loadMetaLevelAttributes();
			Map<Integer, TypeAttribute> metaGroupAttributes = reader.loadMetaGroupAttributes();
			LOG.info("		Meta Types...");
			Map<Integer, MetaType> metaTypes = reader.loadMetaTypes();
			LOG.info("		Meta Groups...");
			Map<Integer, MetaGroup> metaGroups = reader.loadMetaGroups();
			LOG.info("		Materials...");
			Map<Integer, List<TypeMaterial>> typeMaterials = reader.loadTypeMaterials();
			LOG.info("		Blueprints...");
			Map<Integer, Blueprint> blueprints =  reader.loadBlueprints();
			LOG.info("	EveCentral: Validating...");
			Set<Integer> marketIDs = new HashSet<>();
			for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
				if (entry.getValue().getMarketGroupID() != 0) {
					marketIDs.add(entry.getKey());
				}
			}
			UniverseApi esi = new UniverseApi();
			Set<Integer> blacklist = EveCentralTest.testEveCentral(marketIDs);
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			int esiOK = 0;
			int esiIgnore = 0;
			int esiError = 0;
			for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
				Element node = xmldoc.createElementNS(null, "row");
				Integer typeID = entry.getKey();
				Type type = entry.getValue();
				Group group = groupIDs.get(type.getGroupID());
				Category category = categories.get(group.getCategoryID());
				if (((category.isPublished() && !category.getName().equals("Celestial"))
						|| type.isPublished()
						|| group.isPublished()
						|| typeID == 27 //Office
						|| typeID == 3468 //Plastic wrapper
						|| typeID == 60 //Asset Safety Wrap
						|| type.getGroupID() == 186 //Wrecks
						) && !category.getName().equals("Infantry")) {
					node.setAttributeNS(null, "id", String.valueOf(typeID));
					final String typeName;
					if (type.getName() != null) {
						typeName = type.getName();
					} else {
						try {
							List<UniverseNamesResponse> list = esi.postUniverseNames(Collections.singletonList(typeID), "tranquility");
							typeName = list.get(0).getName();
							if (typeName.startsWith("[no messageID: ")) {
								esiIgnore++;
								continue;
							}
							esiOK++;
						} catch (ApiException ex) {
							esiError++;
							continue;
						}
					}
					String typeNameFixed = typeName.replace("  ", " ").replace("\t", " ");
					node.setAttributeNS(null, "name", typeNameFixed);
					if (!typeNameFixed.equals(typeName)) {
						spacedItems.add(typeName.replace("  ", " \\S").replace("\t", "\\t"));
					}
					node.setAttributeNS(null, "group", group.getName());
					node.setAttributeNS(null, "category", category.getName());
					node.setAttributeNS(null, "price", intFormat.format(type.getBasePrice()));
					node.setAttributeNS(null, "volume", String.valueOf(type.getVolume()));
			//META -> DB
					int metaLevel = 0;
					TypeAttribute typeAttribute = metaLevelAttributes.get(typeID);
					if (typeAttribute != null) {
						metaLevel = get(typeAttribute);
					}
					String techLevel = "Tech I";
					MetaType metaType = metaTypes.get(typeID);
					TypeAttribute metaGroupAttribute = metaGroupAttributes.get(typeID);
					if (metaType != null) {
						MetaGroup metaGroup = metaGroups.get(metaType.getMetaGroupID());
						if (metaGroup != null) {
							techLevel = metaGroup.getMetaGroupName();
						}
					} else if (metaGroupAttribute != null) {
						MetaGroup metaGroup = metaGroups.get(get(metaGroupAttribute));
						if (metaGroup != null) {
							techLevel = metaGroup.getMetaGroupName();
						}
					}
					node.setAttributeNS(null, "meta", String.valueOf(metaLevel));
					node.setAttributeNS(null, "tech", techLevel);
					node.setAttributeNS(null, "pi", category.getName().equals("Planetary Commodities") || category.getName().equals("Planetary Resources") ? "true" : "false");
					node.setAttributeNS(null, "portion", String.valueOf(type.getPortionSize()));
			//Product ID -> DB
					int productTypeID = 0;
					int productQuantity = 0;
					Blueprint blueprint = blueprints.get(typeID);
					if (blueprint != null) {
						BlueprintActivity manufacturing = blueprint.getActivities().get("manufacturing");
						BlueprintActivity reaction = blueprint.getActivities().get("reaction");
						if (manufacturing != null) {
							List<BlueprintMaterial> products = manufacturing.products;
							if (products != null && products.size() == 1) {
								productTypeID = products.get(0).typeID;
								productQuantity = products.get(0).quantity;
							} else {
								System.out.println("Manufacturing products" + typeName + " products: " + products);
							}
						} else if (reaction != null) {
							List<BlueprintMaterial> products = reaction.products;
							if (products != null && products.size() == 1) {
								productTypeID = products.get(0).typeID;
								productQuantity = products.get(0).quantity;
							} else {
								System.out.println("Reaction products" + typeName + " products: " + products);
							}
						}
					}
					node.setAttributeNS(null, "product", String.valueOf(productTypeID));
					if (productQuantity > 1) {
						node.setAttributeNS(null, "productquantity", String.valueOf(productQuantity));
					}
					boolean bMarketGroup = (type.getMarketGroupID() != 0);
					boolean blacklisted = blacklist.contains(typeID);
					node.setAttributeNS(null, "marketgroup", String.valueOf(bMarketGroup && !blacklisted));
					if (bMarketGroup && blacklisted) {
						blacklistedItems.add(typeName);
					}
					parentNode.appendChild(node);
					List<TypeMaterial> materials = typeMaterials.get(typeID);
					if (materials != null) {
						Collections.sort(materials, new Comparator<TypeMaterial>() {
							@Override
							public int compare(TypeMaterial o1, TypeMaterial o2) {
								return Integer.compare(o1.getMaterialTypeID(), o2.getMaterialTypeID());
							}
						});
						for (TypeMaterial material : materials) {
							Element materialNode = xmldoc.createElementNS(null, "material");
							materialNode.setAttributeNS(null, "id", String.valueOf(material.getMaterialTypeID()));
							materialNode.setAttributeNS(null, "quantity", String.valueOf(material.getQuantity()));
							materialNode.setAttributeNS(null, "portionsize", String.valueOf(type.getPortionSize()));
							if (type.getMarketGroupID() != 0){
								node.appendChild(materialNode);
							}
						}
					}
				}
			}
			LOG.info("		Blacklisted Items: " + blacklistedItems.size());
			if (blacklist.isEmpty()) {
				LOG.info("			none");
			}
			LOG.info("		Items contains too much space: ");
			for (String name : spacedItems) {
				LOG.info("			" + name);
			}
			if (spacedItems.isEmpty()) {
				LOG.info("			none");
			}
			LOG.info("		ESI requests: " + esiOK + " successful, " + esiError + " unsuccessful, " + esiIgnore + " ignored");
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
			return false;
		}
	}

	private int get(TypeAttribute typeAttribute) {
		if (typeAttribute.getValueInt() != 0) {
			return typeAttribute.getValueInt();
		} else if (typeAttribute.getValueFloat() != 0) {
			return Math.round(typeAttribute.getValueFloat());
		} else {
			return 0;
		}
	}
}
