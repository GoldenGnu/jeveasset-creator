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

package net.nikr.eve.io.creator.impl.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.online.EveCentralTest;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ItemsSql extends AbstractXmlWriter implements Creator {
	
	private final static Logger LOG = LoggerFactory.getLogger(ItemsSql.class);
	
	@Override
	public boolean create() {
		LOG.info("Items:");
		boolean success = false;
		try {
			Document xmldoc = getXmlDocument("rows");
			LOG.info("	Creating...");
			Comment comment = xmldoc.createComment("Generated from Eve Online Toolkit. ©CCP hf. All rights reserved. Used with permission.");
			xmldoc.getDocumentElement().appendChild(comment);
			success = createItems(xmldoc);
			LOG.info("	Saving...");
			writeXmlFile(xmldoc, Program.getFilename(getFilename()));
		} catch (XmlException ex) {
			LOG.error("Items not saved (XML): "+ex.getMessage(), ex);
			return false;
		}
		LOG.info("	Items done");
		return success;
	}

	@Override
	public String getFilename() {
		return "sql"+File.separator+"items.xml";
	}

	@Override
	public String getName() {
		return "Items (SQL)";
	}

	private Set<Integer> makeEveCentralList() throws XmlException {
		Statement stmt;
		String query;
		ResultSet rs;
		Connection connection = Program.openConnection();
		Set<Integer> typeIDs = new HashSet<Integer>();
		try {
			stmt = connection.createStatement();
			query = "SELECT"
					+ " invTypes.typeID"
					+ " ,invTypes.marketGroupID"
					+ " FROM "
					+ " invTypes"
					+ " LEFT JOIN invMarketGroups ON invTypes.marketGroupID = invMarketGroups.marketGroupID"
					/*
					+ " WHERE"
					+ " ((invCategories.published = 1 AND invCategories.categoryName != 'Celestial')"
					+ " OR invTypes.published = 1"
					+ " OR invGroups.published = 1"
					+ " OR invTypes.typeID = 3468" //Plastic wrapper
					+ " OR invTypes.typeID = 27)" //Office
					+ " AND invCategories.categoryName != 'Infantry'" //Office
					*/
					;
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return null;
			}
			while (rs.next()) {
				if (rs.getInt("marketGroupID") != 0) {
					typeIDs.add(rs.getInt("typeID"));
				}
			}
			Program.close(rs);
			Program.close(stmt);

		} catch (SQLException ex) {
			throw new XmlException(ex);
		} finally {
			Program.close(connection);
		}
		return typeIDs;
	}

	private boolean createItems(Document xmldoc) throws XmlException {
		LOG.info("		Testing on EveCentral...");
		Set<Integer> blacklist = new HashSet<Integer>();
		blacklist = EveCentralTest.testEveCentral(makeEveCentralList());
		Set<String> blacklistedItems = new HashSet<String>();
		Statement stmt;
		String query;
		ResultSet rs;
		Connection connection = Program.openConnection();
		Element parentNode = xmldoc.getDocumentElement();
		try {
			stmt = connection.createStatement();
			query = "SELECT marketGroupID FROM invMarketGroups WHERE marketGroupName = 'Planetary Materials'";
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return false;
			}
			int planetaryMaterialsID = 0;
			while (rs.next()) {
				planetaryMaterialsID = rs.getInt("marketGroupID");
			}
			Program.close(rs);
			Program.close(stmt);

			stmt = connection.createStatement();
			query = "SELECT COUNT(invTypes.typeID) as count"
					+ " FROM"
					+ " invTypes"
					+ " LEFT JOIN invGroups ON invTypes.groupID = invGroups.groupID"
					+ " LEFT JOIN invCategories ON invGroups.categoryID = invCategories.categoryID"
					+ " WHERE"
					+ " ((invCategories.published = 1 AND invCategories.categoryName != 'Celestial')"
					+ " OR invTypes.published = 1"
					+ " OR invGroups.published = 1"
					+ " OR invTypes.typeID = 3468" //Plastic wrapper
					+ " OR invTypes.typeID = 27)" //Office
					+ " AND invCategories.categoryName != 'Infantry'" //Office
					;
					
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return false;
			}
			int realCount = 0;
			while (rs.next()) {
				realCount = rs.getInt("count");
			}
			Program.close(rs);
			Program.close(stmt);

			stmt = connection.createStatement();
			query = "SELECT"
					+ " invTypes.typeID"
					+ " ,invTypes.volume"
					+ " ,invTypes.typeName"
					+ " ,invTypes.basePrice"
					+ " ,invTypes.marketGroupID"
					+ " ,invTypes.portionSize"
					+ " ,invGroups.groupName"
					+ " ,invCategories.categoryName"
					+ " ,invCategories.published"
					+ " ,invMetaGroups.metaGroupName"
					+ " ,invMarketGroups.parentGroupID"
					+ " FROM "
					+ " invTypes"
					+ " LEFT JOIN invGroups ON invTypes.groupID = invGroups.groupID"
					+ " LEFT JOIN invCategories ON invGroups.categoryID = invCategories.categoryID"
					+ " LEFT JOIN invMetaTypes ON invTypes.typeID = invMetaTypes.typeID"
					+ " LEFT JOIN invMetaGroups ON invMetaTypes.metaGroupID = invMetaGroups.metaGroupID"
					+ " LEFT JOIN invMarketGroups ON invTypes.marketGroupID = invMarketGroups.marketGroupID"
					+ " WHERE"
					+ " ((invCategories.published = 1 AND invCategories.categoryName != 'Celestial')"
					+ " OR invTypes.published = 1"
					+ " OR invGroups.published = 1"
					+ " OR invTypes.typeID = 3468" //Plastic wrapper
					+ " OR invTypes.typeID = 27)" //Office
					+ " AND invCategories.categoryName != 'Infantry'" //Office
					;
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return false;
			}
			int count = 0;
			while (rs.next()) {
				Element node = xmldoc.createElementNS(null, "row");
				int id = rs.getInt("typeID");
				node.setAttributeNS(null, "id", String.valueOf(id));
				String name = rs.getString("typeName");
				name = name.replace("  ", " ");
				name = name.replace("\t", " ");
				node.setAttributeNS(null, "name", name);
				node.setAttributeNS(null, "group", rs.getString("groupName"));
				node.setAttributeNS(null, "category", rs.getString("categoryName"));
				node.setAttributeNS(null, "price", String.valueOf(rs.getLong("basePrice")));
				node.setAttributeNS(null, "volume", String.valueOf(rs.getDouble("volume")));
				String tech = rs.getString("metaGroupName");
				if (tech == null) {
					tech = "Tech I";
				}
				node.setAttributeNS(null, "meta", String.valueOf(getMetaLevel(connection, id)));
				node.setAttributeNS(null, "tech", tech);
				node.setAttributeNS(null, "pi", rs.getInt("parentGroupID") == planetaryMaterialsID ? "true" : "false");
				node.setAttributeNS(null, "portion", String.valueOf(rs.getInt("portionSize")));
				node.setAttributeNS(null, "product", String.valueOf(getProductTypeID(connection, id)));

				int nMarketGroup = rs.getInt("marketGroupID");
				boolean bMarketGroup = (nMarketGroup != 0);
				boolean blacklisted = blacklist.contains(id);
				node.setAttributeNS(null, "marketgroup", String.valueOf(bMarketGroup && !blacklisted));
				if (bMarketGroup && blacklisted) {
					blacklistedItems.add(name);
				}
				parentNode.appendChild(node);

				addMaterials(connection, xmldoc, node, id, rs.getInt("portionSize"));

				count++;
			}
			Program.close(rs);
			Program.close(stmt);
			if (realCount != count){
				return false;
			}
		} catch (SQLException ex) {
			throw new XmlException(ex);
		} finally {
			Program.close(connection);
		}
		//StringBuilder builder = new StringBuilder();
		//boolean first = true;
		LOG.info("Blacked items: ");
		for (String name : blacklistedItems) {
			LOG.info(name);
			/*
			if (first) {
				first = false;
			} else {
				builder.append(", ");
			}
			builder.append(name);
			*/
		}
		//LOG.info("Blacked items are: " + builder.toString());
		return true;
	}

	private int getMetaLevel(Connection connection, int id){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			String query = "SELECT * FROM dgmTypeAttributes"
				+ " WHERE attributeID = 633 AND typeID = " + id;
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return 0;
			}
			while (rs.next()) {
				int metaLevel = 0;
				float valueFloat = rs.getFloat("valueFloat");
				if (valueFloat != 0){
					metaLevel = Math.round(valueFloat);
				}
				int valueInt = rs.getInt("valueInt");
				if (valueInt != 0){
					metaLevel = valueInt;
				}
				return metaLevel;
			}
		} catch (SQLException ex) {
			LOG.error("Items not saved (SQL): "+ex.getMessage(), ex);
		} finally {
			Program.close(rs);
			Program.close(stmt);
		}
		return 0;
	}

	private int getProductTypeID(Connection connection, int typeID){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			//String query = "SELECT productTypeID FROM industryActivityProducts WHERE typeID = "+typeID + " AND activityID = 1";
			String query = "SELECT productTypeID FROM invBlueprintTypes WHERE blueprintTypeID = "+typeID;
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return 0;
			}
			while (rs.next()) {
				return rs.getInt("productTypeID");
			}
		} catch (SQLException ex) {
			LOG.error("Materials not added (SQL): "+ex.getMessage(), ex);
		} finally {
			Program.close(rs);
			Program.close(stmt);
		}
		return 0;
	}

	private void addMaterials(Connection connection, Document xmldoc, Element parentNode, int typeID, int portionSize){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.createStatement();
			String query = "SELECT * FROM invTypeMaterials WHERE typeID = "+typeID+ " ORDER BY materialTypeID DESC"; // AND typeID >= 34 AND typeID <= 40
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return;
			}
			while (rs.next()) {
				int requiredTypeID = rs.getInt("materialTypeID");
				int quantity = rs.getInt("quantity");
				Element node = xmldoc.createElementNS(null, "material");
				node.setAttributeNS(null, "id", String.valueOf(requiredTypeID));
				node.setAttributeNS(null, "quantity", String.valueOf(quantity));
				node.setAttributeNS(null, "portionsize", String.valueOf(portionSize));
				if (isMarketItem(connection, typeID)){
					parentNode.appendChild(node);
				}
			}
		} catch (SQLException ex) {
			LOG.error("Materials not added (SQL): "+ex.getMessage(), ex);
		} finally {
			Program.close(rs);
			Program.close(stmt);
		}
	}
	private boolean isMarketItem(Connection connection, int typeID){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			/*
			groupID != 0

			//BAD
			&& groupID != 268
			&& groupID != 269
			&& groupID != 270
			&& groupID != 332

			//Unknown
			&& (groupID == 428
			|| groupID == 530)

			//Good
			&& groupID != 18
			&& groupID == 873
			&& groupID == 429
			&& groupID == 280
			&& groupID == 334
			&& groupID == 333
			&& groupID == 754
			&& groupID == 886
			&& groupID == 913
			&& groupID == 964
			&& groupID == 423
			*/
			stmt = connection.createStatement();
			String query = "SELECT * FROM invTypes WHERE typeID = "+typeID+" AND marketGroupID IS NOT NULL AND groupID != 0 AND groupID != 268 AND groupID != 269 AND groupID != 270 AND groupID != 332";
			rs = stmt.executeQuery(query);
			if (rs == null) {
				return false;
			}
			while (rs.next()) {
				return true;
			}
		} catch (SQLException ex) {
			LOG.error("Name not added (SQL): "+ex.getMessage(), ex);
		} finally {
			Program.close(rs);
			Program.close(stmt);
		}
		return false;
	}
}