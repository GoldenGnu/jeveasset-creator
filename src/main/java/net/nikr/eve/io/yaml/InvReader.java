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

package net.nikr.eve.io.yaml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.nikr.eve.io.data.inv.Blueprint;
import net.nikr.eve.io.data.inv.Category;
import net.nikr.eve.io.data.inv.DogmaTypes;
import net.nikr.eve.io.data.inv.DogmaAttribute;
import net.nikr.eve.io.data.inv.DogmeEffect;
import net.nikr.eve.io.data.inv.Group;
import net.nikr.eve.io.data.inv.MetaGroup;
import net.nikr.eve.io.data.inv.Type;
import net.nikr.eve.io.data.inv.TypeMaterial;
import net.nikr.eve.io.yaml.YamlHelper.SdeFile;


public class InvReader {
	public Map<Long, Type> loadTypes() throws IOException {
		return YamlHelper.read(SdeFile.TYPEIDS, new TypeReference<TreeMap<Long, Type>>(){});
	}

	public Map<Long, Group> loadGroups() throws IOException {
		return YamlHelper.read(SdeFile.GROUPIDS, new TypeReference<TreeMap<Long, Group>>(){});
	}

	public Map<Long, Category> loadCategories() throws IOException {
		return YamlHelper.read(SdeFile.CATEGORYIDS, new TypeReference<TreeMap<Long, Category>>(){});
	}

	public Attributes loadDogma() throws IOException {
		TreeMap<Long, DogmaTypes> map = YamlHelper.read(SdeFile.TYPEDOGMA, new TypeReference<TreeMap<Long, DogmaTypes>>(){});
		Attributes attributes = new Attributes();
		for (Map.Entry<Long, DogmaTypes> entry : map.entrySet()) {
			long typeID = entry.getKey();
			DogmaTypes dogma = entry.getValue();
			for (DogmaAttribute attribute : dogma.getDogmaAttributes()) {
				if (attribute.getAttributeID() == 1692) { //1692 = meta group
					attributes.getMetaGroupAttributes().put(typeID, attribute.getValue().longValue());
				}
				if (attribute.getAttributeID() == 633) { //633 = meta level
					attributes.getMetaLevelAttributes().put(typeID, attribute.getValue().longValue());
				}
				if (attribute.getAttributeID() == 128) { //128 = The size of the charges that can fit in the turret/whatever.
					attributes.getChargesSize().put(typeID, attribute.getValue().longValue());
				}
			}
			for (DogmeEffect attribute : dogma.getDogmaEffects()) {
				if (attribute.isIsDefault()) {
					continue;
				}
				if (attribute.getEffectID() == 11) { //11 = Requires a low power slot
					attributes.getSlots().put(typeID, "Low");
				}
				if (attribute.getEffectID() == 12) { //12 = Requires a high power slot
					attributes.getSlots().put(typeID, "High");
				}
				if (attribute.getEffectID() == 13) { //13 = Requires a medium power slot
					attributes.getSlots().put(typeID, "Medium");
				}
				if (attribute.getEffectID() == 2663) { //2663 = Must be installed into an open rig slot
					attributes.getSlots().put(typeID, "Rig");
				}
				if (attribute.getEffectID() == 3772) { //3772 = Must be installed into an available subsystem slot on a Tech III ship.
					attributes.getSlots().put(typeID, "Subsystem");
				}
			}
		}
		return attributes;
	}

	public Map<Long, MetaGroup> loadMetaGroups() throws IOException {
		return YamlHelper.read(SdeFile.METAGROUPS, new TypeReference<TreeMap<Long, MetaGroup>>(){});
	}

	public Map<Long, TypeMaterialList> loadTypeMaterials() throws IOException {
		return YamlHelper.read(SdeFile.INVTYPEMATERIALS, new TypeReference<TreeMap<Long, TypeMaterialList>>(){});
	}

	public Map<Long, Blueprint> loadBlueprints() throws IOException {
		return YamlHelper.read(SdeFile.BLUEPRINTS, new TypeReference<TreeMap<Long, Blueprint>>(){});
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TypeMaterialList {
		private List<TypeMaterial> materials = new ArrayList<>();

		public List<TypeMaterial> getMaterials() {
			return materials;
		}
	}

	public static class Attributes {
		private final Map<Long, Long> metaGroupAttributes = new HashMap<>();
		private final Map<Long, Long> metaLevelAttributes = new HashMap<>();
		private final Map<Long, Long> chargesSize = new HashMap<>();
		private final Map<Long, String>  slots = new HashMap<>();

		public Map<Long, Long> getMetaGroupAttributes() {
			return metaGroupAttributes;
		}

		public Map<Long, Long> getMetaLevelAttributes() {
			return metaLevelAttributes;
		}

		public Map<Long, Long> getChargesSize() {
			return chargesSize;
		}

		public Map<Long, String> getSlots() {
			return slots;
		}
	}
}
