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
		Document xmldoc = null;
		boolean success = false;
		try {
			xmldoc = getXmlDocument("rows");
			success = createItems(xmldoc, con);
			writeXmlFile(xmldoc, Program.getFilename("items.xml"));
		} catch (XmlException ex) {
			Log.error("Items not saved (XML): "+ex.getMessage(), ex);
		}
		Log.info("Items saved");
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
				+ " dbo.invTypes.typeID"
				+ " ,dbo.invTypes.volume"
				+ " ,dbo.invTypes.typeName"
				+ " ,dbo.invTypes.basePrice"
				+ " ,dbo.invTypes.marketGroupID"
				+ " ,dbo.invGroups.groupName"
				+ " ,dbo.invCategories.categoryName"
				+ " ,dbo.invMetaGroups.metaGroupName"
				+ " FROM "
				+ " dbo.invTypes LEFT JOIN dbo.invGroups ON invTypes.groupID = invGroups.groupID"
				+ " LEFT JOIN dbo.invCategories ON dbo.invGroups.categoryID = dbo.invCategories.categoryID"
				+ " LEFT JOIN dbo.invMetaTypes ON dbo.invTypes.typeID = dbo.invMetaTypes.typeID"
				+ " LEFT JOIN dbo.invMetaGroups ON dbo.invMetaTypes.metaGroupID = dbo.invMetaGroups.metaGroupID"
				+ " ORDER BY dbo.invTypes.typeID" ;
			rs = stmt.executeQuery(query);
			if (rs == null) return false;
			int count = 0;
			while (rs.next()) {
				Element node = xmldoc.createElementNS(null, "row");
				int id = rs.getInt("typeID");
				node.setAttributeNS(null, "id", String.valueOf(id));
				node.setAttributeNS(null, "name", rs.getString("typeName"));
				node.setAttributeNS(null, "group", rs.getString("groupName"));
				node.setAttributeNS(null, "category", rs.getString("categoryName"));
				node.setAttributeNS(null, "price", String.valueOf(rs.getLong("basePrice")));
				node.setAttributeNS(null, "volume", String.valueOf(rs.getDouble("volume")));
				node.setAttributeNS(null, "meta", getMetaLevel(con, id, rs.getString("metaGroupName")));

				int nMarketGroup = rs.getInt("marketGroupID");
				boolean bMarketGroup = (nMarketGroup != 0);
				node.setAttributeNS(null, "marketgroup", String.valueOf(bMarketGroup));
				parentNode.appendChild(node);

				addMaterials(con, xmldoc, node, id);

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
			String query = "SELECT * FROM dbo.dgmTypeAttributes"
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
					metaLevel =  String.valueOf(valueInt);
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

	private void addMaterials(Connection con, Document xmldoc, Element parentNode, int typeID){
		Statement stmt = null;
		String query = "";
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			query = "SELECT * FROM typeActivityMaterials  WHERE typeID = "+typeID+" AND activityID = 6";
			rs = stmt.executeQuery(query);
			if (rs == null) return;
			while (rs.next()) {
				int requiredTypeID = rs.getInt("requiredTypeID");
				int quantity = rs.getInt("quantity");

				Element node = xmldoc.createElementNS(null, "material");
				node.setAttributeNS(null, "materialid", String.valueOf(requiredTypeID));
				node.setAttributeNS(null, "quantity", String.valueOf(quantity));
				parentNode.appendChild(node);
			}
		} catch (SQLException ex) {
			return;
		}
	}

  @Override
  public String getName() {
    return "Items";
  }
}