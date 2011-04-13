/*
 * Copyright 2009, Niklas Kyster Rasmussen, Flaming Candle
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
import net.nikr.eve.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Items extends AbstractXmlWriter implements Creator {
	@Override
	public void create(File f, Connection con) {
		saveItems(con);
	}

	public boolean saveItems(Connection con){
		Log.info("Items:");
		Document xmldoc = null;
		boolean success = false;
		try {
			xmldoc = getXmlDocument("rows");
			Log.info("	Creating...");
			success = createItems(xmldoc, con);
			Log.info("	Saving...");
			writeXmlFile(xmldoc, Program.getFilename("data"+File.separator+"items.xml"));
		} catch (XmlException ex) {
			Log.error("Items not saved (XML): "+ex.getMessage(), ex);
		}
		Log.info("	Items done");
		return success;
	}

	private boolean createItems(Document xmldoc, Connection con) throws XmlException {

		Statement stmt = null;
		String query = "";
		ResultSet rs = null;
		Element parentNode = xmldoc.getDocumentElement();
		try {
			stmt = con.createStatement();
			query = "SELECT COUNT(*) as count FROM invTypes";
			rs = stmt.executeQuery(query);
			if (rs == null) return false;
			int realCount = 0;
			while (rs.next()) {
				realCount = rs.getInt("count");
			}

			stmt = con.createStatement();
			query = "SELECT"
				+ " invTypes.typeID"
				+ " ,invTypes.volume"
				+ " ,invTypes.typeName"
				+ " ,invTypes.basePrice"
				+ " ,invTypes.marketGroupID"
				+ " ,invTypes.portionSize"
				+ " ,invGroups.groupName"
				+ " ,invCategories.categoryName"
				+ " ,invMetaGroups.metaGroupName"
				+ " FROM "
				+ " invTypes LEFT JOIN invGroups ON invTypes.groupID = invGroups.groupID"
				+ " LEFT JOIN invCategories ON invGroups.categoryID = invCategories.categoryID"
				+ " LEFT JOIN invMetaTypes ON invTypes.typeID = invMetaTypes.typeID"
				+ " LEFT JOIN invMetaGroups ON invMetaTypes.metaGroupID = invMetaGroups.metaGroupID"
				+ " ORDER BY invTypes.typeID" ;
			rs = stmt.executeQuery(query);
			if (rs == null) return false;
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
				node.setAttributeNS(null, "meta", getMetaLevel(con, id, rs.getString("metaGroupName")));

				int nMarketGroup = rs.getInt("marketGroupID");
				boolean bMarketGroup = (nMarketGroup != 0);
				node.setAttributeNS(null, "marketgroup", String.valueOf(bMarketGroup));
				parentNode.appendChild(node);

				addMaterials(con, xmldoc, node, id, rs.getInt("portionSize"));

				count++;
			}
			if (realCount != count){
				return false;
			}

		} catch (SQLException ex) {
			throw new XmlException(ex);
		}
		return true;
	}

	private String getMetaLevel(Connection con, int id, String metaGroupName){
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			String query = "SELECT * FROM dgmTypeAttributes"
				+ " WHERE attributeID = 633 AND typeID = " + id;
			rs = stmt.executeQuery(query);
			if (rs == null) return "";
			while (rs.next()) {
				String metaLevel = "";
				float valueFloat = rs.getFloat("valueFloat");
				if (valueFloat != 0){
					metaLevel = String.valueOf( Math.round(valueFloat) );
				}
				int valueInt = rs.getInt("valueInt");
				if (valueInt != 0){
					metaLevel = String.valueOf(valueInt);
				}
				if (metaGroupName != null) {
					return metaLevel+" ("+metaGroupName+")";
				} else {
					return metaLevel;
				}
			}
		} catch (SQLException ex) {
			Log.error("Items not saved (SQL): "+ex.getMessage(), ex);
		}
		return "";

	}

	private void addMaterials(Connection con, Document xmldoc, Element parentNode, int typeID, int portionSize){
		Statement stmt = null;
		String query = "";
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			query = "SELECT * FROM invTypeMaterials WHERE typeID = "+typeID; // AND typeID >= 34 AND typeID <= 40
			rs = stmt.executeQuery(query);
			if (rs == null) return;
			while (rs.next()) {
				int requiredTypeID = rs.getInt("materialTypeID");
				int quantity = rs.getInt("quantity");
				Element node = xmldoc.createElementNS(null, "material");
				node.setAttributeNS(null, "id", String.valueOf(requiredTypeID));
				node.setAttributeNS(null, "quantity", String.valueOf(quantity));
				node.setAttributeNS(null, "portionsize", String.valueOf(portionSize));
				if (isMarketItem(con, typeID)){
					parentNode.appendChild(node);
				}
			}
		} catch (SQLException ex) {
			Log.error("Materials not added (SQL): "+ex.getMessage(), ex);
		}
	}
	private boolean isMarketItem(Connection con, int typeID){
		Statement stmt = null;
		String query = "";
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
			stmt = con.createStatement();
			query = "SELECT * FROM invTypes WHERE typeID = "+typeID+" AND marketGroupID IS NOT NULL AND groupID != 0 AND groupID != 268 AND groupID != 269 AND groupID != 270 AND groupID != 332";
			rs = stmt.executeQuery(query);
			if (rs == null) return false;
			while (rs.next()) {
				return true;
			}

		} catch (SQLException ex) {
			Log.error("Name not added (SQL): "+ex.getMessage(), ex);
		}
		return false;
	}

	@Override
	public String getName() {
		return "Items";
	}
}