/*
 * Copyright 2009-2023 Contributors (see credits.txt)
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
package net.nikr.eve.io.creator.impl;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import net.nikr.eve.io.data.inv.Type;
import net.nikr.eve.io.data.inv.TypeMaterial;
import net.nikr.eve.io.esi.EsiUpdater;
import static net.nikr.eve.io.esi.EsiUpdater.DATASOURCE;
import static net.nikr.eve.io.esi.EsiUpdater.MARKET_API;
import net.nikr.eve.io.esi.EsiUpdater.TypeData;
import static net.nikr.eve.io.esi.EsiUpdater.UNIVERSE_API;
import net.nikr.eve.io.esi.EsiUpdater.Update;
import net.nikr.eve.io.esi.EsiUpdater.UpdatePage;
import net.nikr.eve.io.esi.EsiUpdater.UpdateType;
import net.nikr.eve.io.esi.EsiUpdater.UpdateValues;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.InvReader;
import net.nikr.eve.io.yaml.InvReader.Attributes;
import net.nikr.eve.io.yaml.InvReader.TypeMaterialList;
import net.nikr.eve.util.Duration;
import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.ApiResponse;
import net.troja.eve.esi.model.MarketGroupResponse;
import net.troja.eve.esi.model.TypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Items extends AbstractXmlWriter implements Creator{

	private static final Logger LOG = LoggerFactory.getLogger(Items.class);

	private static final int EXPECTED_META_GROUPS_SIZE = 13; //On Change: Check if jEveAssets EsiItemsGetter needs to be updated!

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
			writeXmlFile(xmldoc, getFile());
			duration.end();
			LOG.info("	Items completed in " + duration.getString());
			return success;
		} catch (XmlException ex) {
			LOG.error("Items not saved (XML): "+ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	public String getName() {
		return "items.xml";
	}

	@Override
	public File getFile() {
		return Program.getDataFile("items.xml");
	}

	private boolean createItems(Document xmldoc) {
		NumberFormat intFormat = new DecimalFormat("0");
		try {
			Set<Integer> missingNames = new HashSet<>();
			Map<String, String> spacedItems = new HashMap<>();
			Set<String> techLevelItems = new HashSet<>();
			Set<String> productsItems = new HashSet<>();
			LOG.info("	YAML: Loading...");
			InvReader reader = new InvReader();
			LOG.info("		Types...");
			Map<Integer, Type> typeIDs = reader.loadTypes();
			LOG.info("		Groups...");
			Map<Integer, Group> groupIDs = reader.loadGroups();
			LOG.info("		Categories...");
			Map<Integer, Category> categories = reader.loadCategories();
			LOG.info("		Attributes...");
			Attributes attributes = reader.loadDogma();
			LOG.info("		Meta Groups...");
			Map<Integer, MetaGroup> metaGroups = reader.loadMetaGroups();
			LOG.info("		Materials...");
			Map<Integer, TypeMaterialList> typeMaterials = reader.loadTypeMaterials();
			LOG.info("		Blueprints...");
			Map<Integer, Blueprint> blueprints =  reader.loadBlueprints();
			LOG.info("	ESI: Loading...");
			LOG.info("		Market Groups...");
			Set<Integer> marketGroupsTypeIDs = getMarketGroupsTypeIDs();
			LOG.info("		Types...");
			Set<Integer> types = getTypes();
			LOG.info("		Packaged Volume...");
			Map<Integer, Float> volume = getVolume(typeIDs, types, categories, groupIDs);
			LOG.info("		Proving Filaments...");
			updateProvingFilaments(typeIDs, types, groupIDs, categories);
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			if (metaGroups.size() != EXPECTED_META_GROUPS_SIZE) {
				throw new RuntimeException("metaGroups size is: " + metaGroups.size() + " expected: " + EXPECTED_META_GROUPS_SIZE + " :: jEveAssets EsiItemsGetter likely needs to be updated!");
			}
			for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
				Element node = xmldoc.createElement("row");
				Integer typeID = entry.getKey();
				Type type = entry.getValue();
				Group group = groupIDs.get(type.getGroupID());
				Category category = categories.get(group.getCategoryID());
				if (((category.isPublished() && !category.getEnglishName().equals("Celestial"))
						|| type.isPublished()
						|| group.isPublished()
						|| typeID == 27 //Office
						|| typeID == 3468 //Plastic wrapper
						|| typeID == 60 //Asset Safety Wrap
						|| type.getGroupID() == 186 //Wrecks
						) && !category.getEnglishName().equals("Infantry")) {
					node.setAttribute("id", String.valueOf(typeID));
					final String typeName;
					if (type.getEnglishName() != null) {
						typeName = type.getEnglishName();
					} else {
						missingNames.add(typeID);
						continue;
					}
					//Normalize Names
					String typeNameFixed = typeName
							.replaceAll(" +", " ") //Replace 2 or more spaces
							.replace("\t", " ") //Tab
							.replace("„", "\"") //Index
							.replace("“", "\"") //Set transmit state
							.replace("”", "\"") //Cancel character
							.replace("‘", "'") //Private use one
							.replace("’", "'") //Private use two
							.replace("`", "'") //Grave accent
							.replace("´", "'") //Acute accent
							.replace("–", "-") //En dash
							.replace("‐", "-") //Hyphen
							.replace("‑", "-") //Non-breaking hyphen
							.replace("‒", "-") //Figure dash
							.replace("—", "-") //Em dash
							.trim();
					if (!typeNameFixed.equals(typeName)) {
						spacedItems.put(typeName, typeNameFixed);
					}
					node.setAttribute("name", typeNameFixed);
					node.setAttribute("group", group.getEnglishName());
					node.setAttribute("category", category.getEnglishName());
					node.setAttribute("price", intFormat.format(type.getBasePrice()));
					node.setAttribute("volume", String.valueOf(type.getVolume()));
			//Packaged Volume
					Float packagedVolume = volume.get(typeID);
					if (packagedVolume != null) {
						node.setAttribute("packagedvolume", String.valueOf(packagedVolume));
					}
			//Capacity
					if (type.getCapacity() > 0) {
						node.setAttribute("capacity", String.valueOf(type.getCapacity()));
					}
			//Tech Level
					final String techLevel;
					MetaGroup metaGroup = null;
					//Get meta group from type
					Integer metaGroupID =  type.getMetaGroupID();
					if (metaGroup == null && metaGroupID != null) {
						metaGroup = metaGroups.get(metaGroupID);
					}
					//Get meta group from attributes
					metaGroupID = attributes.getMetaGroupAttributes().get(typeID);
					if (metaGroup == null && metaGroupID != null) {
						metaGroup = metaGroups.get(metaGroupID);
					}
					if (metaGroup != null) {
						techLevel = metaGroup.getMetaGroupName().replace("Structure", "").trim();
					} else {
						techLevel = "Tech I";
					}
					node.setAttribute("tech", techLevel);
			//Slot
					String slot = attributes.getSlots().get(typeID);
					if (slot != null) {
						node.setAttribute("slot", slot);
					}
			//Charge Size
					Integer chargesSize = attributes.getChargesSize().get(typeID);
					if (chargesSize != null) {
						node.setAttribute("charges", String.valueOf(chargesSize));
					}
			//Meta Level
					//Ref: https://www.eveonline.com/news/view/deciphering-tiericide
					int metaLevel = attributes.getMetaLevelAttributes().getOrDefault(typeID, 0);
					node.setAttribute("meta", String.valueOf(metaLevel));
					node.setAttribute("pi", category.getEnglishName().equals("Planetary Commodities") || category.getEnglishName().equals("Planetary Resources") ? "true" : "false");
					node.setAttribute("portion", String.valueOf(type.getPortionSize()));
			//Product ID
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
								productsItems.add("Manufacturing products: " + typeName + " products: " + products);
							}
							if (manufacturing.materials != null) {
								for (BlueprintMaterial material : manufacturing.materials) {
									Element materialNode = xmldoc.createElement("mfg");
									materialNode.setAttribute("id", String.valueOf(material.getTypeID()));
									materialNode.setAttribute("q", String.valueOf(material.getQuantity()));
									node.appendChild(materialNode);
								}
							}
						} else if (reaction != null) {
							List<BlueprintMaterial> products = reaction.products;
							if (products != null && products.size() == 1) {
								productTypeID = products.get(0).typeID;
								productQuantity = products.get(0).quantity;
							} else {
								productsItems.add("Reaction products: " + typeName + " products: " + products);
							}
							if (reaction.materials != null) {
								for (BlueprintMaterial material : reaction.materials) {
									Element materialNode = xmldoc.createElement("rxn");
									materialNode.setAttribute("id", String.valueOf(material.getTypeID()));
									materialNode.setAttribute("q", String.valueOf(material.getQuantity()));
									node.appendChild(materialNode);
								}
							}
						}
					}
					node.setAttribute("product", String.valueOf(productTypeID));
					if (productQuantity > 1) {
						node.setAttribute("productquantity", String.valueOf(productQuantity));
					}
					boolean bMarketGroup = marketGroupsTypeIDs.contains(typeID);
					node.setAttribute("marketgroup", String.valueOf(bMarketGroup));
					parentNode.appendChild(node);
					TypeMaterialList materialList = typeMaterials.get(typeID);
					if (materialList != null) {
						List<TypeMaterial> materials = materialList.getMaterials();
						Collections.sort(materials, new Comparator<TypeMaterial>() {
							@Override
							public int compare(TypeMaterial o1, TypeMaterial o2) {
								return Integer.compare(o1.getMaterialTypeID(), o2.getMaterialTypeID());
							}
						});
						for (TypeMaterial material : materials) {
							Element materialNode = xmldoc.createElement("material");
							materialNode.setAttribute("id", String.valueOf(material.getMaterialTypeID()));
							materialNode.setAttribute("quantity", String.valueOf(material.getQuantity()));
							materialNode.setAttribute("portionsize", String.valueOf(type.getPortionSize()));
							if (type.getMarketGroupID() != 0){
								node.appendChild(materialNode);
							}
						}
					}
				}
			}
			LOG.info("		Items contains too much space:");
			for (Map.Entry<String, String> entry : spacedItems.entrySet()) {
				LOG.info("			|" + entry.getKey().replace(" ", "•").replace("	", "→") + "|" + entry.getValue() + "|"); //¶→●•∙·
			}
			LOG.info("		Items with \"strange\" tech levels:");
			for (String name : techLevelItems) {
				LOG.info("			" + name);
			}
			LOG.info("		Items missing products:");
			for (String name : productsItems) {
				LOG.info("			" + name);
			}
			if (spacedItems.isEmpty()) {
				LOG.info("			none");
			}
			LOG.info("		Items missing names:");
			for (Integer typeID : missingNames) {
				LOG.info("			" + typeID);
			}
			if (missingNames.isEmpty()) {
				LOG.info("			none");
			}
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
			return false;
		}
	}

	private Set<Integer> getMarketGroupsTypeIDs() {
		Set<Integer> typeIDs = new HashSet<>();
		List<Integer> marketsGroups = EsiUpdater.update(new Update<List<Integer>>() {
			@Override
			public ApiResponse<List<Integer>> update() throws ApiException {
				return MARKET_API.getMarketsGroupsWithHttpInfo(DATASOURCE, null);
			}
		});
		List<Update<MarketGroupResponse>> updates = new ArrayList<>();
		for (Integer marketGroup : marketsGroups) {
			updates.add(new Update<MarketGroupResponse>() {
				@Override
				public ApiResponse<MarketGroupResponse> update() throws ApiException {
					return MARKET_API.getMarketsGroupsMarketGroupIdWithHttpInfo(marketGroup, null, DATASOURCE, null, null);
				}
			});
		}
		List<MarketGroupResponse> responses = EsiUpdater.update(updates);
		for (MarketGroupResponse response : responses) {
			typeIDs.addAll(response.getTypes());
		}
		return typeIDs;
	}

	private Set<Integer> getTypes() {
		Set<Integer> data = new HashSet<>();
		List<List<Integer>> responses = EsiUpdater.updatePage(new UpdatePage<List<Integer>>() {
			@Override
			public ApiResponse<List<Integer>> update(int page) throws ApiException {
				return UNIVERSE_API.getUniverseTypesWithHttpInfo(DATASOURCE, null, page);
			}
		});
		for (List<Integer> h : responses) {
			data.addAll(h);
		}
		return data;
	}

	private Map<Integer, Float> getVolume(Map<Integer, Type> typeIDs, Set<Integer> types, Map<Integer, Category> categories, Map<Integer, Group> groupIDs) {
		List<UpdateType> updates = new ArrayList<>();
		Set<String> skipped = new HashSet<>();
		for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
			if (!types.contains(entry.getKey())) {
				skipped.add(entry.getKey().toString());
				continue;
			}
			Group group = groupIDs.get(entry.getValue().getGroupID());
			Category category = categories.get(group.getCategoryID());
			if (category.getEnglishName().equals("Ship") || category.getEnglishName().equals("Module") || category.getEnglishName().equals("Celestial")) {
				updates.add(new UpdateType(entry.getKey()));
			}
		}
		List<UpdateValues<TypeResponse, Integer>> responses = EsiUpdater.updateValues(updates);
		Map<Integer, Float> volume = new HashMap<>();
		for (UpdateValues<TypeResponse, Integer> response : responses) {
			TypeData typeData = new TypeData(response);
			if (typeData.havePackagedVolume()) {
				volume.put(typeData.getTypeID(), typeData.getPackagedVolume());
			}
		}
		LOG.warn("			Skipped: " + String.join(",", skipped));
		return volume;
	}

	private void updateProvingFilaments(Map<Integer, Type> typeIDs, Set<Integer> types, Map<Integer, Group> groupIDs, Map<Integer, Category> categories) {
		List<UpdateType> updates = new ArrayList<>();
		Set<String> skipped = new HashSet<>();
		for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
			if (!types.contains(entry.getKey())) {
				skipped.add(entry.getKey().toString());
				continue;
			}
			Type type = entry.getValue();
			Group group = groupIDs.get(type.getGroupID());
			Category category = categories.get(group.getCategoryID());
			if (group.getEnglishName().equalsIgnoreCase("Abyssal Proving Filaments")
					|| (category.getEnglishName().equalsIgnoreCase("Apparel") && type.getEnglishName().contains(".png"))
					) {
				updates.add(new UpdateType(entry.getKey()));
			}
		}
		List<UpdateValues<TypeResponse, Integer>> responses = EsiUpdater.updateValues(updates);
		for (UpdateValues<TypeResponse, Integer> response : responses) {
			TypeData typeData = new TypeData(response);
			if (typeData.haveName()) {
				typeIDs.get(typeData.getTypeID()).setEnglishName(typeData.getName());
			}
		}
		LOG.warn("			Skipped: " + String.join(",", skipped));
	}
}
