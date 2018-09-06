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

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.nikr.eve.io.data.inv.Blueprint;
import net.nikr.eve.io.data.inv.BlueprintActivity;
import net.nikr.eve.io.data.inv.BlueprintMaterial;
import net.nikr.eve.io.data.inv.BlueprintSkill;
import net.nikr.eve.io.data.inv.Category;
import net.nikr.eve.io.data.inv.Group;
import net.nikr.eve.io.data.inv.MetaGroup;
import net.nikr.eve.io.data.inv.MetaType;
import net.nikr.eve.io.data.inv.Type;
import net.nikr.eve.io.data.inv.TypeAttribute;
import net.nikr.eve.io.data.inv.TypeMaterial;
import net.nikr.eve.io.yaml.YamlHelper.SdeFile;


public class InvReader {
	public Map<Integer, Type> loadTypes() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.TYPEIDS);
		return YamlHelper.convert(reader.read(TypeMap.class, Type.class));
	}

	public Map<Integer, Group> loadGroups() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.GROUPIDS);
		return YamlHelper.convert(reader.read(GroupMap.class, Group.class));
	}

	public Map<Integer, Category> loadCategories() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.CATEGORYIDS);
		return YamlHelper.convert(reader.read(CategoryMap.class, Category.class));
	}

	public Attributes loadAttributes() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.DGMTYPEATTRIBUTES);
		List<TypeAttribute> list = reader.read(TypeAttributeList.class, TypeAttribute.class);
		Attributes attributes = new Attributes();
		for (TypeAttribute value : list) {
			if (value.getAttributeID() == 422) { //422 = tech level
				attributes.getTechLevelAttributes().put(value.getTypeID(), value);
			}
			if (value.getAttributeID() == 633) { //633 = meta level
				attributes.getMetaLevelAttributes().put(value.getTypeID(), value);
			}
			if (value.getAttributeID() == 1692) { //1692 = meta group
				attributes.getMetaGroupAttributes().put(value.getTypeID(), value);
			}
		}
		return attributes;
	}

	public Map<Integer, MetaType> loadMetaTypes() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.INVMETATYPES);
		List<MetaType> list = reader.read(MetaTypeList.class, MetaType.class);
		Map<Integer, MetaType> map = new HashMap<Integer, MetaType>();
		for (MetaType value : list) {
			map.put(value.getTypeID(), value);
		}
		return map;
	}

	public Map<Integer, MetaGroup> loadMetaGroups() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.INVMETAGROUPS);
		List<MetaGroup> list = reader.read(MetaGroupList.class, MetaGroup.class);
		Map<Integer, MetaGroup> map = new HashMap<Integer, MetaGroup>();
		for (MetaGroup value : list) {
			map.put(value.getMetaGroupID(), value);
		}
		return map;
	}

	public Map<Integer, List<TypeMaterial>> loadTypeMaterials() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.INVTYPEMATERIALS);
		List<TypeMaterial> list = reader.read(TypeMaterialList.class, TypeMaterial.class);
		Map<Integer, List<TypeMaterial>> map = new HashMap<Integer, List<TypeMaterial>>();
		for (TypeMaterial value : list) {
			List<TypeMaterial> materials = map.get(value.getTypeID());
			if (materials == null) {
				materials = new ArrayList<TypeMaterial>();
				map.put(value.getTypeID(), materials);
			}
			materials.add(value);
		}
		return map;
	}

	public Map<Integer, Blueprint> loadBlueprints() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.BLUEPRINTS);
		reader.getConfig().setPropertyElementType(Blueprint.class, "activities", BlueprintActivity.class);
		reader.getConfig().setPropertyElementType(BlueprintActivity.class, "materials", BlueprintMaterial.class);
		reader.getConfig().setPropertyElementType(BlueprintActivity.class, "products", BlueprintMaterial.class);
		reader.getConfig().setPropertyElementType(BlueprintActivity.class, "skills", BlueprintSkill.class);
		return YamlHelper.convert(reader.read(BlueprintMap.class, Blueprint.class));
	}

	public static class TypeMap extends TreeMap<String, Type> {}
	public static class GroupMap extends TreeMap<String, Group> { }
	public static class CategoryMap extends TreeMap<String, Category> { }
	public static class TypeAttributeList extends ArrayList<TypeAttribute> { }
	public static class MetaTypeList extends ArrayList<MetaType> { }
	public static class MetaGroupList extends ArrayList<MetaGroup> { }
	public static class TypeMaterialList extends ArrayList<TypeMaterial> { }
	public static class BlueprintMap extends TreeMap<String, Blueprint> { }

	public static class Attributes {
		private final Map<Integer, TypeAttribute> metaLevelAttributes = new HashMap<>();
		private final Map<Integer, TypeAttribute> metaGroupAttributes = new HashMap<>();
		private final Map<Integer, TypeAttribute> techLevelAttributes = new HashMap<>();

		public Map<Integer, TypeAttribute> getMetaLevelAttributes() {
			return metaLevelAttributes;
		}

		public Map<Integer, TypeAttribute> getMetaGroupAttributes() {
			return metaGroupAttributes;
		}

		public Map<Integer, TypeAttribute> getTechLevelAttributes() {
			return techLevelAttributes;
		}
	}
}
