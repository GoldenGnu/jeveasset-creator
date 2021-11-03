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
import java.util.Date;
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
import net.nikr.eve.io.data.inv.DogmaAttribute;
import net.nikr.eve.io.data.inv.TypeMaterial;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.InvReader;
import net.nikr.eve.io.yaml.InvReader.TypeMaterialList;
import net.nikr.eve.util.Duration;
import net.troja.eve.esi.ApiClient;
import net.troja.eve.esi.ApiClientBuilder;
import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.ApiResponse;
import net.troja.eve.esi.HeaderUtil;
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
	private static final int EXPECTED_META_GROUPS_SIZE = 13; //On Change: Check if jEveAssets EsiItemsGetter needs to be updated!

	private static final String DATASOURCE = "tranquility";

	private static Integer errorLimit = null;
	private static Date errorReset = new Date();

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
			LOG.info("	YAML: Loading...");
			InvReader reader = new InvReader();
			LOG.info("		Types...");
			Map<Integer, Type> typeIDs = reader.loadTypes();
			LOG.info("		Groups...");
			Map<Integer, Group> groupIDs = reader.loadGroups();
			LOG.info("		Categories...");
			Map<Integer, Category> categories = reader.loadCategories();
			LOG.info("		Attributes...");
			Map<Integer, DogmaAttribute> metaGroupAttributes = reader.loadAttributes();
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
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			if (metaGroups.size() != EXPECTED_META_GROUPS_SIZE) {
				throw new RuntimeException("metaGroups size is: " + metaGroups.size() + " expected: " + EXPECTED_META_GROUPS_SIZE + " :: jEveAssets EsiItemsGetter likely needs to be updated!");
			}
			for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
				Element node = xmldoc.createElementNS(null, "row");
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
					node.setAttributeNS(null, "id", String.valueOf(typeID));
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
						spacedItems.add(typeName);
					}
					node.setAttributeNS(null, "name", typeNameFixed);
					node.setAttributeNS(null, "group", group.getEnglishName());
					node.setAttributeNS(null, "category", category.getEnglishName());
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
			//Tech Level
					final String techLevel;
					MetaGroup metaGroup = null;
					//Get meta group from type
					Integer metaGroupID =  type.getMetaGroupID();
					if (metaGroup == null && metaGroupID != null) {
						metaGroup = metaGroups.get(metaGroupID);
					}
					//Get meta group from attributes
					DogmaAttribute metaGroupAttribute = metaGroupAttributes.get(typeID);
					if (metaGroup == null && metaGroupAttribute != null) {
						metaGroupID = get(metaGroupAttribute);
						metaGroup = metaGroups.get(metaGroupID);
					}
					if (metaGroup != null) {
						if (metaGroup.getMetaGroupName().contains("Structure")) {
							techLevel = metaGroup.getMetaGroupName().replace("Structure", "").trim();
						} else {
							techLevel = metaGroup.getMetaGroupName();
						}
					} else {
						techLevel = "Tech I";
					}
					node.setAttributeNS(null, "tech", techLevel);
			//Meta level (~ Meta group ID)
					int metaLevel = 0;
					if (metaGroupID != null) {
						switch (metaGroupID) {
							case 52: metaLevel = 4; break; //Structure Faction
							case 53: metaLevel = 2; break; //Structure Tech II
							case 54: metaLevel = 1; break; //Structure Tech I
							default: metaLevel = metaGroupID;
						}
					}
					node.setAttributeNS(null, "meta", String.valueOf(metaLevel));
					node.setAttributeNS(null, "pi", category.getEnglishName().equals("Planetary Commodities") || category.getEnglishName().equals("Planetary Resources") ? "true" : "false");
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
							if (manufacturing.materials != null) {
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
							if (reaction.materials != null) {
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

	private int get(DogmaAttribute typeAttribute) {
		if (typeAttribute.getValue() != null) {
			return typeAttribute.getValue().intValue();
		} else {
			return 0;
		}
	}

	public static <K> List<Future<K>> startReturn(Collection<? extends Callable<K>> updaters) {
		ExecutorService executor = Executors.newFixedThreadPool(50);
		try {
			return executor.invokeAll(updaters);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
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
				checkErrors(); //Update timeframe as needed
				ApiResponse<MarketGroupResponse> response = MARKET_API.getMarketsGroupsMarketGroupIdWithHttpInfo(marketGroup, null, DATASOURCE, null, null);
				setErrorLimit(response.getHeaders());
				return response.getData();
			} catch (ApiException ex) {
				setErrorLimit(ex.getResponseHeaders());
				retries++;
				if (retries <= 3) {
					LOG.warn("Failed to update market group:" + marketGroup + " " + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody(), ex);
					try {
						Thread.sleep(retries * 1000);
					} catch (InterruptedException ex1) {
						//No problem
					}
					return update();
				} else {
					LOG.error("Failed to update market group:" + marketGroup + " " + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody(), ex);
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
	}

	private Set<Integer> getTypes() {
		Set<Integer> data = new HashSet<>();
		UpdateTypes updateTypes = new UpdateTypes(1);
		ApiResponse<List<Integer>> response = updateTypes.update();
		data.addAll(response.getData());
		Integer xPages = HeaderUtil.getXPages(response.getHeaders());
		if (xPages == null || xPages < 2) {
			throw new RuntimeException("xPages is " + xPages);
		}
		List<UpdateTypes> updates = new ArrayList<>();
		for (int page = 2; page <= xPages; page++) {
			updates.add(new UpdateTypes(page));
		}
		List<Future<List<Integer>>> futures = startReturn(updates);
		for (Future<List<Integer>> future : futures) {
			try {
				data.addAll(future.get());
			} catch (InterruptedException | ExecutionException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		}
		return data;
	}

	private static class UpdateTypes implements Callable<List<Integer>> {

		private final int page;
		private int retries = 0;

		public UpdateTypes(int page) {
			this.page = page;
		}
		
		@Override
		public List<Integer> call() throws Exception {
			return update().getData();
		}

		public ApiResponse<List<Integer>> update() {
			try {
				checkErrors(); //Update timeframe as needed
				ApiResponse<List<Integer>> response = UNIVERSE_API.getUniverseTypesWithHttpInfo(DATASOURCE, null, page);
				setErrorLimit(response.getHeaders());
				return response;
			} catch (ApiException ex) {
				setErrorLimit(ex.getResponseHeaders());
				retries++;
				if (retries <= 3) {
					LOG.warn("Failed to update page:" + page + " " + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody());
					try {
						Thread.sleep(retries * 1000);
					} catch (InterruptedException ex1) {
						//No problem
					}
					return update();
				} else {
					LOG.error("Failed to update page:" + page + " " + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody(), ex);
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
	}

	private Map<Integer, Float> getVolume(Map<Integer, Type> typeIDs, Set<Integer> types, Map<Integer, Category> categories, Map<Integer, Group> groupIDs) {
		List<UpdateVolume> updates = new ArrayList<>();
		Set<String> skipped = new HashSet<>();
		for (Map.Entry<Integer, Type> entry : typeIDs.entrySet()) {
			if (!types.contains(entry.getKey())) {
				skipped.add(entry.getKey().toString());
				continue;
			}
			Group group = groupIDs.get(entry.getValue().getGroupID());
			Category category = categories.get(group.getCategoryID());
			if (category.getEnglishName().equals("Ship") || category.getEnglishName().equals("Module") || category.getEnglishName().equals("Celestial")) {
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
		LOG.warn("			Skipped: " + String.join(",", skipped));
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
				checkErrors(); //Update timeframe as needed
				ApiResponse<TypeResponse> response = UNIVERSE_API.getUniverseTypesTypeIdWithHttpInfo(typeID, null, DATASOURCE, null, null);
				setErrorLimit(response.getHeaders());
				return response.getData();
			} catch (ApiException ex) {
				setErrorLimit(ex.getResponseHeaders());
				retries++;
				if (ex.getCode() == 404) {
					LOG.warn("typeID:" + typeID + " not found");
					return null;
				} else if (retries <= 3) {
					try {
						Thread.sleep(retries * 1000);
					} catch (InterruptedException ex1) {
						//No problem
					}
					LOG.warn("Failed to update typeID:" + typeID + " " + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody());
					return update();
				} else {
					LOG.error("Failed to update typeID:" + typeID + " " + ex.getMessage() + "\r\n" + ex.getCode() + " " + ex.getResponseBody(), ex);
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
			return response != null
					&& response.getPackagedVolume() != null
					&& response.getVolume() != null
					&& !response.getVolume().equals(response.getPackagedVolume());
					
		}

		private Float getPackagedVolume() {
			if (response != null) {
				return response.getPackagedVolume();
			} else {
				return 0f;
			}
		}
	}

	private synchronized static void setErrorLimit(Map<String, List<String>> responseHeaders) {
		if (responseHeaders != null) {
			Integer limit = getHeaderInteger(responseHeaders, "x-esi-error-limit-remain");
			if (limit != null) {
				if (errorLimit != null) {
					errorLimit = Math.min(errorLimit, limit);
				} else {
					errorLimit = limit;
				}
			}
			Integer reset = getHeaderInteger(responseHeaders, "x-esi-error-limit-reset");
			if (reset != null) {
				errorReset = new Date(System.currentTimeMillis() + (reset * 1000L));
			}
		}
	}

	private synchronized static void checkErrors() {
		if (errorLimit != null && errorLimit <= 50) { //Error limit reached
			try {
				long wait = (errorReset.getTime() + 1000) - System.currentTimeMillis();
				LOG.warn("Error limit reached waiting: " + milliseconds(wait));
				if (wait > 0) { //Negative values throws an Exception
					Thread.sleep(wait); //Wait until the error window is reset
				}
				//Reset
				errorReset = new Date(); //New timeframe
				errorLimit = null;  //No errors in this timeframe (yet)
			} catch (InterruptedException ex) {
				//No problem
			}
		} else if (errorLimit != null && errorLimit < 100) { //At least one error
			LOG.warn("Error limit: " + errorLimit);
		}
	}

	private static String milliseconds(long durationInMillis) {
		long millis = durationInMillis % 1000;
		long second = (durationInMillis / 1000) % 60;

		return String.format("%02ds %dms", second, millis);
	}

	private static Integer getHeaderInteger(Map<String, List<String>> responseHeaders, String headerName) {
		String errorResetHeader = HeaderUtil.getHeader(responseHeaders, headerName);
		if (errorResetHeader != null) {
			try {
				return Integer.valueOf(errorResetHeader);
			} catch (NumberFormatException ex) {
				//No problem
			}
		}
		return null;
	}
}
