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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.dogma.DogmaType;
import net.nikr.eve.io.data.dogma.DogmaType.Attribute;
import net.nikr.eve.io.data.dogma.DogmeAttribute;
import net.nikr.eve.io.data.dogma.DogmeLocalization;
import net.nikr.eve.io.esi.EsiUpdater;
import net.nikr.eve.io.esi.EsiUpdater.TypeData;
import net.nikr.eve.io.esi.EsiUpdater.UpdateType;
import net.nikr.eve.io.esi.EsiUpdater.UpdateValues;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.YamlHelper;
import net.nikr.eve.util.Duration;
import net.troja.eve.esi.model.TypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Dogma extends AbstractXmlWriter implements Creator{

	private static final Logger LOG = LoggerFactory.getLogger(Dogma.class);

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public boolean create() {
		Duration duration = new Duration();
		duration.start();
		LOG.info("Dogma:");
		try {
			LOG.info("	XML: init...");
			Document xmldoc = getXmlDocument("rows");
			Comment comment = xmldoc.createComment("Generated from Eve Online Toolkit. ©CCP hf. All rights reserved. Used with permission.");
			xmldoc.getDocumentElement().appendChild(comment);
			boolean success = createItems(xmldoc);
			LOG.info("	XML: Saving...");
			writeXmlFile(xmldoc, getFile());
			duration.end();
			LOG.info("	Dogma completed in " + duration.getString());
			return success;
		} catch (XmlException ex) {
			LOG.error("Dogma not saved (XML): "+ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	public String getName() {
		return "dogma.xml";
	}

	@Override
	public File getFile() {
		return Program.getDataFile("dogma.xml");
	}

	private boolean createItems(Document xmldoc) {
		try {
			LOG.info("	YAML: Loading...");
			LOG.info("		Attributes...");
			Map<Integer, DogmeAttribute> dogmaTypes = YamlHelper.read(YamlHelper.SdeFile.DOGMAATTRIBUTES, new TypeReference<TreeMap<Integer, DogmeAttribute>>(){});
			LOG.info("	Hoboleaks: Loading...");
			LOG.info("		Dynamic Attributes...");
			Map<Integer, DogmaType> values = MAPPER.readValue(new URL("https://sde.hoboleaks.space/tq/dynamicitemattributes.json"), new TypeReference<HashMap<Integer, DogmaType>>() {});
			LOG.info("		Attributes Localization...");
			Localization localization = new Localization(MAPPER.readValue(new URL("https://sde.hoboleaks.space/tq/localization_dgmattributes.json"), new TypeReference<HashMap<Integer, HashMap<String, DogmeLocalization>>>() {}));
			LOG.info("	ESI: Loading...");
			LOG.info("		Type Dogma...");
			Map<Integer, TypeData> typies = update(values);
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			Map<Integer, String> names = new HashMap<>();

			Map<Integer, List<Attribute>> t = new HashMap<>();
			Map<Integer, Set<Integer>> applicableTypes = new HashMap<>();
			for (Map.Entry<Integer, DogmaType> entry : values.entrySet()) {
				DogmaType type = entry.getValue();
				Integer typeID = type.getTypeID();
				List<Attribute> current = t.get(typeID);
				if (current == null) {
					current = type.getAttributes();
				} else { //Compare
					current = compare(current, type.getAttributes());
				}
				t.put(typeID, current);
				Set<Integer> set = applicableTypes.get(typeID);
				if (set == null) {
					set = new HashSet<>();
					applicableTypes.put(typeID, set);
				}
				set.addAll(type.getApplicableTypes());
			}
			
			Element typesNode = xmldoc.createElement("types");
			for (Map.Entry<Integer, List<Attribute>> entry : t.entrySet()) {
				Integer typeID = entry.getKey();
				Element typeNode = xmldoc.createElement("type");
				typesNode.appendChild(typeNode);
				typeNode.setAttribute("id", String.valueOf(typeID));
				Map<Integer, Map<Integer, Double>> max = getValues(typies, applicableTypes.get(typeID), true);
				Map<Integer, Map<Integer, Double>> min = getValues(typies, applicableTypes.get(typeID), false);
				for (Attribute attribute : entry.getValue()) {
					final int attributeID = attribute.attributeID;
					TotalAttribute totalAttribute = create(attribute, dogmaTypes.get(attributeID));
					Element attributeNode = xmldoc.createElement("attr");
					names.put(attributeID, localization.getLocalization(attributeID).getName());
					attributeNode.setAttribute("id", String.valueOf(attributeID));
					attributeNode.setAttribute("max", String.valueOf(totalAttribute.max));
					attributeNode.setAttribute("min", String.valueOf(totalAttribute.min));
					attributeNode.setAttribute("high", String.valueOf(totalAttribute.highIsGood));
					typeNode.appendChild(attributeNode);
					Set<Integer> meta = new HashSet<>();
					meta.addAll(max.get(attributeID).keySet());
					meta.addAll(min.get(attributeID).keySet());
					for (Integer metaGroupID: meta) {
						Element defaultsNode = xmldoc.createElement("default");
						defaultsNode.setAttribute("meta", String.valueOf(metaGroupID));
						defaultsNode.setAttribute("dmax", String.valueOf(max.get(attributeID).get(metaGroupID)));
						defaultsNode.setAttribute("dmin", String.valueOf(min.get(attributeID).get(metaGroupID)));
						attributeNode.appendChild(defaultsNode);
					}
					names.put(attributeID, localization.getLocalization(attributeID).getName());
				}
			}
			parentNode.appendChild(typesNode);

			Element namesNode = xmldoc.createElement("attributes");
			for (Map.Entry<Integer, String> entry : names.entrySet()) {
				Element nameNode = xmldoc.createElement("attribute");
				nameNode.setAttribute("id", String.valueOf(entry.getKey()));
				nameNode.setAttribute("name", entry.getValue());
				namesNode.appendChild(nameNode);
			}
			parentNode.appendChild(namesNode);

			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
			return false;
		}
	}

	private List<Attribute> compare(List<Attribute> a, List<Attribute> b) {
		Map<Integer, Attribute> lookupA = new HashMap<>();
		
		for (Attribute attribute : a) {
			lookupA.put(attribute.getAttributeID(), attribute);
		}
		Map<Integer, Attribute> lookupB = new HashMap<>();
		for (Attribute attribute : b) {
			lookupB.put(attribute.getAttributeID(), attribute);
		}
		Set<Integer> attributesIDs = new HashSet<>();
		attributesIDs.addAll(lookupA.keySet());
		attributesIDs.addAll(lookupB.keySet());
		List<Attribute> attributes = new ArrayList<>();
		for (Integer attributesID : attributesIDs) {
			Attribute attributeA = lookupA.get(attributesID);
			Attribute attributeB = lookupB.get(attributesID);
			attributes.add(compare(attributeB, attributeA));
		}
		return attributes;
	}

	private Attribute compare(Attribute a, Attribute b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		double max = Math.max(a.max, b.max);
		double min = Math.min(a.min, b.min);
		return new Attribute(a.attributeID, max, min);
	}

	private Map<Integer, Map<Integer, Double>> getValues(Map<Integer, TypeData> typies, Set<Integer> applicableTypes, boolean max) {
		Map<Integer, Map<Integer, Double>> values = new HashMap<>();
		for (Integer typeID : applicableTypes) {
			TypeData typeData = typies.get(typeID);
			Integer metaGroupID = typeData.getMetaGroupID();
			for (Map.Entry<Integer, Float> entry : typeData.getAttributes().entrySet()) {
				Integer attributeID = entry.getKey();
				Double value = (double)entry.getValue();
				Map<Integer, Double> map = values.get(attributeID);
				if (map == null) {
					map = new HashMap<>();
					values.put(attributeID, map);
				}
				Double current = map.get(metaGroupID);
				if (current == null || current.equals(value)) {
					current = value;
				} else { //Not same
					if (max) {
						current = Math.max(current, value);
					} else {
						current = Math.min(current, value);
					}
				}
				map.put(metaGroupID, current);
			}
		}
		return values;
	}

	private TotalAttribute create(Attribute attribute, DogmeAttribute dogmeAttribute) {
		return new TotalAttribute(attribute.attributeID, attribute.max, attribute.min, dogmeAttribute.highIsGood);
	}

	private static class Localization {
		HashMap<Integer, DogmeLocalization> localization = new HashMap<>();

		public Localization(HashMap<Integer, HashMap<String, DogmeLocalization>> localization) {
			for (HashMap.Entry<Integer, HashMap<String, DogmeLocalization>> entry : localization.entrySet()) {
				int attributeID = entry.getKey();
				DogmeLocalization localizationDogme = entry.getValue().get("en-us");
				this.localization.put(attributeID, localizationDogme);
			}
		}

		public DogmeLocalization getLocalization(int attributeID) {
			return this.localization.get(attributeID);
		}
	}

	private Map<Integer, TypeData> update(Map<Integer, DogmaType> values) {
		Set<Integer> typeIDs = new HashSet<>();
		for (Map.Entry<Integer, DogmaType> entry : values.entrySet()) {
			DogmaType type = entry.getValue();
			typeIDs.addAll(type.getApplicableTypes());
		}

		List<UpdateType> updates = new ArrayList<>();
		for (int typeID : typeIDs) {
			updates.add(new UpdateType(typeID));
		}
		List<UpdateValues<TypeResponse, Integer>> responses = EsiUpdater.updateValues(updates);
		Map<Integer, TypeData> types = new HashMap<>();
		for (UpdateValues<TypeResponse, Integer> response : responses) {
			types.put(response.getValue(), new TypeData(response));
		}
		return types;
	}

	public static class TotalAttribute {
		private int attributeID;
		private double max;
		private double min;
		private boolean highIsGood;

		public TotalAttribute(int attributeID, double max, double min, boolean highIsGood) {
			this.attributeID = attributeID;
			this.max = max;
			this.min = min;
			this.highIsGood = highIsGood;
		}
	}
}
