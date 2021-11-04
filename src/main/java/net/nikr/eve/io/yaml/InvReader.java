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

package net.nikr.eve.io.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.nikr.eve.io.data.inv.Blueprint;
import net.nikr.eve.io.data.inv.Category;
import net.nikr.eve.io.data.inv.Dogma;
import net.nikr.eve.io.data.inv.Group;
import net.nikr.eve.io.data.inv.MetaGroup;
import net.nikr.eve.io.data.inv.Type;
import net.nikr.eve.io.data.inv.DogmaAttribute;
import net.nikr.eve.io.data.inv.TypeMaterial;
import net.nikr.eve.io.yaml.YamlHelper.SdeFile;


public class InvReader {
	public Map<Integer, Type> loadTypes() throws IOException {
		return YamlHelper.read(SdeFile.TYPEIDS, new TypeReference<TreeMap<Integer, Type>>(){});
	}

	public Map<Integer, Group> loadGroups() throws IOException {
		return YamlHelper.read(SdeFile.GROUPIDS, new TypeReference<TreeMap<Integer, Group>>(){});
	}

	public Map<Integer, Category> loadCategories() throws IOException {
		return YamlHelper.read(SdeFile.CATEGORYIDS, new TypeReference<TreeMap<Integer, Category>>(){});
	}

	public Attributes loadDogma() throws IOException {
		TreeMap<Integer, Dogma> map = YamlHelper.read(SdeFile.TYPEDOGMA, new TypeReference<TreeMap<Integer, Dogma>>(){});
		Attributes attributes = new Attributes();
		for (Map.Entry<Integer, Dogma> entry : map.entrySet()) {
			int typeID = entry.getKey();
			Dogma dogma = entry.getValue();
			for (DogmaAttribute attribute : dogma.getDogmaAttributes()) {
				if (attribute.getAttributeID() == 1692) { //1692 = meta group
					attributes.getMetaGroupAttributes().put(typeID, attribute.getValue().intValue());
				}
				if (attribute.getAttributeID() == 633) { //633 = meta level
					attributes.getMetaLevelAttributes().put(typeID, attribute.getValue().intValue());
				}
			}
		}
		return attributes;
	}

	public Map<Integer, MetaGroup> loadMetaGroups() throws IOException {
		return YamlHelper.read(SdeFile.METAGROUPS, new TypeReference<TreeMap<Integer, MetaGroup>>(){});
	}

	public Map<Integer, TypeMaterialList> loadTypeMaterials() throws IOException {
		return YamlHelper.read(SdeFile.INVTYPEMATERIALS, new TypeReference<TreeMap<Integer, TypeMaterialList>>(){});
	}

	public Map<Integer, Blueprint> loadBlueprints() throws IOException {
		return YamlHelper.read(SdeFile.BLUEPRINTS, new TypeReference<TreeMap<Integer, Blueprint>>(){});
	}

	public static class TypeMaterialList {
		private List<TypeMaterial> materials;

		public List<TypeMaterial> getMaterials() {
			return materials;
		}
	}

	public static class Attributes {
		private final Map<Integer, Integer> metaGroupAttributes = new HashMap<>();
		private final Map<Integer, Integer> metaLevelAttributes = new HashMap<>();

		public Map<Integer, Integer> getMetaGroupAttributes() {
			return metaGroupAttributes;
		}

		public Map<Integer, Integer> getMetaLevelAttributes() {
			return metaLevelAttributes;
		}
	}
}
