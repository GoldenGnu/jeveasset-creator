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
package net.nikr.eve.io.creator.impl;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.inv.Blueprint;
import net.nikr.eve.io.data.inv.BlueprintActivity;
import net.nikr.eve.io.data.inv.BlueprintMaterial;
import net.nikr.eve.io.data.inv.Category;
import net.nikr.eve.io.data.inv.Group;
import net.nikr.eve.io.data.inv.MetaGroup;
import net.nikr.eve.io.data.inv.Type;
import net.nikr.eve.io.data.inv.TypeAttribute;
import net.nikr.eve.io.data.inv.TypeMaterial;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.InvReader;
import net.nikr.eve.io.yaml.InvReader.TypeMaterialList;
import net.nikr.eve.util.Duration;
import net.troja.eve.esi.ApiClient;
import net.troja.eve.esi.ApiClientBuilder;
import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.api.MarketApi;
import net.troja.eve.esi.api.UniverseApi;
import net.troja.eve.esi.model.MarketGroupResponse;
import net.troja.eve.esi.model.TypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Items extends AbstractXmlWriter implements Creator{

	private static final Logger LOG = LoggerFactory.getLogger(Items.class);

	private static final ApiClient CLIENT = new ApiClientBuilder().userAgent("jEveAssets XML Builder").build();
	private static final UniverseApi UNIVERSE_API = new UniverseApi(CLIENT);
	private static final MarketApi MARKET_API = new MarketApi(CLIENT);
	private static final boolean MATS = false;

	private static final String DATASOURCE = "tranquility";

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
			Set<String> spacedItems = new HashSet<>();
			Set<String> techLevelItems = new HashSet<>();
			Set<String> productsItems = new HashSet<>();
			LOG.info("	MATS: " + MATS);
			LOG.info("	YAML: Loading...");
			InvReader reader = new InvReader();
			LOG.info("		Types...");
			Map<Integer, Type> typeIDs = reader.loadTypes();
			LOG.info("		Groups...");
			Map<Integer, Group> groupIDs = reader.loadGroups();
			LOG.info("		Categories...");
			Map<Integer, Category> categories = reader.loadCategories();
			LOG.info("		Attributes...");
			InvReader.Attributes attributes = reader.loadAttributes();
			Map<Integer, TypeAttribute> metaLevelAttributes = attributes.getMetaLevelAttributes();
			Map<Integer, TypeAttribute> metaGroupAttributes = attributes.getMetaGroupAttributes();
			Map<Integer, TypeAttribute> techLevelAttributes = attributes.getTechLevelAttributes();
			LOG.info("		Meta Groups...");
			Map<Integer, MetaGroup> metaGroups = reader.loadMetaGroups();
			LOG.info("		Materials...");
			Map<Integer, TypeMaterialList> typeMaterials = reader.loadTypeMaterials();
			LOG.info("		Blueprints...");
			Map<Integer, Blueprint> blueprints =  reader.loadBlueprints();
			LOG.info("	ESI: Loading...");
			LOG.info("		Market Groups...");
			Set<Integer> marketGroupsTypeIDs = getMarketGroupsTypeIDs();
			LOG.info("		Packaged Volume...");
			Map<Integer, Float> volume = getVolume(typeIDs, categories, groupIDs);
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
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
						spacedItems.add(typeName);
					}
					node.setAttributeNS(null, "name", typeNameFixed);
					node.setAttributeNS(null, "group", group.getName());
					node.setAttributeNS(null, "category", category.getName());
					node.setAttributeNS(null, "price", intFormat.format(type.getBasePrice()));
					node.setAttributeNS(null, "volume", String.valueOf(type.getVolume()));
			//Packaged Volume
					Float packagedVolume = volume.get(typeID);
					if (packagedVolume != null) {
						node.setAttributeNS(null, "packagedvolume", String.valueOf(packagedVolume));
					}
			//Capacity
					if (type.getCapacity() > 0) {
						node.setAttributeNS(null, "capacity", String.valueOf(type.getCapacity()));
					}
			//Meta level
					int metaLevel = 0;
					TypeAttribute metaLevelAttribute = metaLevelAttributes.get(typeID);
					if (metaLevelAttribute != null) {
						metaLevel = get(metaLevelAttribute);
					}
					node.setAttributeNS(null, "meta", String.valueOf(metaLevel));
			//Tech Level
					TypeAttribute techLevelAttribute  = techLevelAttributes.get(typeID);
					final String techLevel;
					MetaGroup metaGroup = null;
					//From meta type
					Integer metaGroupID =  type.getMetaGroupID();
					if (metaGroup == null && metaGroupID != null) {
						metaGroup = metaGroups.get(metaGroupID);
					}
					//From meta group attribute
					TypeAttribute metaGroupAttribute = metaGroupAttributes.get(typeID);
					if (metaGroup == null && metaGroupAttribute != null) {
						metaGroup = metaGroups.get(get(metaGroupAttribute));
					}
					if (metaGroup != null) {
						if (metaGroup.getMetaGroupName().contains("Structure")) {
							techLevel = metaGroup.getMetaGroupName().replace("Structure", "").trim();
						} else {
							techLevel = metaGroup.getMetaGroupName();
						}
					} else if (techLevelAttribute != null) {
						switch (get(techLevelAttribute)) {
							case 1: techLevel = "Tech I"; break;
							case 2: techLevel = "Tech II"; break;
							case 3: techLevel = "Tech III"; break;
							default: 
								techLevel = "Tech I";
								techLevelItems.add(typeName + ":" + get(techLevelAttribute));
								break;
						}
					} else {
						techLevel = "Tech I";
					}
					node.setAttributeNS(null, "tech", techLevel);
					node.setAttributeNS(null, "pi", category.getName().equals("Planetary Commodities") || category.getName().equals("Planetary Resources") ? "true" : "false");
					node.setAttributeNS(null, "portion", String.valueOf(type.getPortionSize()));
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
							if (MATS && manufacturing.materials != null) {
								for (BlueprintMaterial material : manufacturing.materials) {
									Element materialNode = xmldoc.createElementNS(null, "mfg");
									materialNode.setAttributeNS(null, "id", String.valueOf(material.getTypeID()));
									materialNode.setAttributeNS(null, "q", String.valueOf(material.getQuantity()));
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
							if (MATS && reaction.materials != null) {
								for (BlueprintMaterial material : reaction.materials) {
									Element materialNode = xmldoc.createElementNS(null, "rxn");
									materialNode.setAttributeNS(null, "id", String.valueOf(material.getTypeID()));
									materialNode.setAttributeNS(null, "q", String.valueOf(material.getQuantity()));
									node.appendChild(materialNode);
								}
							}
						}
					}
					node.setAttributeNS(null, "product", String.valueOf(productTypeID));
					if (productQuantity > 1) {
						node.setAttributeNS(null, "productquantity", String.valueOf(productQuantity));
					}
					boolean bMarketGroup = marketGroupsTypeIDs.contains(typeID);
					node.setAttributeNS(null, "marketgroup", String.valueOf(bMarketGroup));
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
			LOG.info("		Items contains too much space:");
			for (String name : spacedItems) {
				LOG.info("			" + name);
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

	private int get(TypeAttribute typeAttribute) {
		if (typeAttribute.getValueFloat() != null) {
			return Math.round(typeAttribute.getValueFloat());
		} else if (typeAttribute.getValueInt() != null) {
			return typeAttribute.getValueInt();
		} else {
			return 0;
		}
	}

	private Set<Integer> getMarketGroupsTypeIDs() {
		Set<Integer> typeIDs = new HashSet<>();
		try {
			List<Integer> marketsGroups = MARKET_API.getMarketsGroups("tranquility", null);
			List<UpdateGroup> updates = new ArrayList<>();
			for (Integer marketGroup : marketsGroups) {
				updates.add(new UpdateGroup(marketGroup));
			}

			List<Future<List<Integer>>> futures = startReturn(updates);
			for (Future<List<Integer>> future : futures) {
				try {
					typeIDs.addAll(future.get());
				} catch (InterruptedException | ExecutionException ex) {
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
			return typeIDs;
		} catch (ApiException ex) {
			throw new RuntimeException("Failed to get marker groups");
		}
	}

	private static class UpdateGroup implements Callable<List<Integer>> {

		private final Integer marketGroup;
		private final List<Integer> typeIDs = new ArrayList<>();
		private int retries = 0;

		public UpdateGroup(Integer marketGroup) {
			this.marketGroup = marketGroup;
		}

		@Override
		public List<Integer> call() throws Exception {
			MarketGroupResponse response = update();
			typeIDs.addAll(response.getTypes());
			return typeIDs;
		}

		private MarketGroupResponse update() {
			try {
				return MARKET_API.getMarketsGroupsMarketGroupId(marketGroup, null, DATASOURCE, null, null);
			} catch (ApiException ex) {
				retries++;
				if (retries <= 3) {
					return update();
				} else {
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
	}

	public static <K> List<Future<K>> startReturn(Collection<? extends Callable<K>> updaters) {
		ExecutorService executor = Executors.newFixedThreadPool(100);
		try {
			return executor.invokeAll(updaters);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private Map<Integer, Float> getVolume(Map<Integer, Type> typeIDs, Map<Integer, Category> categories, Map<Integer, Group> groupIDs) {
		List<UpdateVolume> updates = new ArrayList<>();
		for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
			Group group = groupIDs.get(entry.getValue().getGroupID());
			Category category = categories.get(group.getCategoryID());
			if (category.getName().equals("Ship") || category.getName().equals("Module") || category.getName().equals("Celestial")) {
				updates.add(new UpdateVolume(entry.getKey()));
			}
		}
		List<Future<TypeData>> futures = startReturn(updates);
		Map<Integer, Float> volume = new HashMap<>();
		for (Future<TypeData> future : futures) {
			try {
				TypeData typeData = future.get();
				if (typeData.haveData()) {
					volume.put(typeData.getTypeID(), typeData.getPackagedVolume());
				}
			} catch (InterruptedException | ExecutionException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
		return volume;
	}

	private static class UpdateVolume implements Callable<TypeData> {

		private final int typeID;
		private int retries = 0;

		public UpdateVolume(int typeID) {
			this.typeID = typeID;
		}
		
		@Override
		public TypeData call() throws Exception {
			TypeResponse response = update();
			return new TypeData(typeID, response);
		}

		private TypeResponse update() {
			try {
				return UNIVERSE_API.getUniverseTypesTypeId(typeID, null, DATASOURCE, null, null);
			} catch (ApiException ex) {
				retries++;
				if (retries <= 3) {
					return update();
				} else {
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
	}

	private static class TypeData {
		private final int typeID;
		private final TypeResponse response;

		public TypeData(int typeID, TypeResponse response) {
			this.typeID = typeID;
			this.response = response;
		}

		public int getTypeID() {
			return typeID;
		}

		private boolean haveData() {
			return response.getPackagedVolume() != null
					//&& !response.getPackagedVolume().equals(0f)
					&& !response.getPackagedVolume().equals(response.getVolume());
					
		}

		private Float getPackagedVolume() {
			return response.getPackagedVolume();
		}
	}
}
