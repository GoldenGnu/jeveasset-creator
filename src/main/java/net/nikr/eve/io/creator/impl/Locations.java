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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Locations extends AbstractXmlWriter implements Creator {
	
	private final static Logger LOG = LoggerFactory.getLogger(Locations.class);

	private DecimalFormat securityformater = new DecimalFormat("0.0", new DecimalFormatSymbols(new Locale("en")));

	private final String query = "SELECT"
			+ "  mapd.itemID"
			+ ", mapd.typeID"
			+ ", mapd.groupID as groupID"
			+ ", CASE WHEN mapd.solarSystemID IS NULL THEN 0 ELSE mapd.solarSystemID END AS systemID"
			+ ", CASE WHEN mapSS.security IS NULL THEN mapd.security ELSE mapSS.security END AS security "
			+ ", mapd.regionID"
			+ ", mapd.itemName "
			+ " FROM mapDenormalize as mapd"
			+ " LEFT JOIN mapSolarSystems AS mapSS ON mapd.itemID = mapSS.solarSystemID";
	
	@Override
	public boolean create(File f, Connection con) {
		return saveLocations(con);
	}

	public boolean saveLocations(Connection con) {
		LOG.info("Locations:");
		Document xmldoc;
		boolean success = false;
		try {
			xmldoc = getXmlDocument("rows");
			LOG.info("	Creating...");
			success = createLocations(xmldoc, con);
			LOG.info("	Saving...");
			writeXmlFile(xmldoc, Program.getFilename("data"+File.separator+"locations.xml"));
		} catch (XmlException ex) {
			LOG.error("Locations not saved (XML): " + ex.getMessage(), ex);
		}
		LOG.info("	Locations done");
		return success;
	}

	private boolean createLocations(Document xmldoc, Connection con) throws XmlException {
		Element parentNode = xmldoc.getDocumentElement();
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query
					+ " WHERE mapd.typeID = 5 OR mapd.typeID = 3 OR mapd.groupID = 15" //3 = Region 5 = Solar System
					+ " ORDER BY mapd.itemID");
			if (rs == null) return false;
			while (rs.next()) {
				int itemID = rs.getInt("itemID");
				int typeID = rs.getInt("typeID");
				String itemName = rs.getString("itemName");
				Element node = xmldoc.createElementNS(null, "row");
				//Location (Station/System/Region)
				
				//Station
				int stationID;
				String station;
				if (rs.getInt("groupID") == 15) { //Self is Station
					stationID = itemID;
					station = itemName;
				} else {
					stationID = 0;
					station = "";
				}
				node.setAttributeNS(null, "si", String.valueOf(stationID));
				node.setAttributeNS(null, "s", station);
				//Systems
				int systemID;
				String systemName;
				if (typeID == 5){ //Self is System, use the itemID
					systemID = itemID;
					systemName = itemName;
				} else {
					systemID = rs.getInt("systemID");
					systemName = getName(con, systemID);
				}
				node.setAttributeNS(null, "syi", String.valueOf(systemID));
				node.setAttributeNS(null, "sy", systemName);
				
				//Region
				int regionID;
				String regionName;
				if (typeID == 3) { //Self is Region, use the itemID
					regionID = itemID;
					regionName = itemName;
				} else {
					regionID = rs.getInt("regionID");
					regionName = getName(con, regionID);
				}
				node.setAttributeNS(null, "ri", String.valueOf(regionID));
				node.setAttributeNS(null, "r", regionName);
				//Security
				double security;
				if (typeID == 5) { //System
					security = rs.getDouble("security");
				} else { //Region or Station (Region don't have security AKA 0.0)
					security = rs.getFloat("security");
				}
				node.setAttributeNS(null, "se", roundSecurity(security));
				parentNode.appendChild(node);
			}
		} catch (SQLException ex) {
			throw new XmlException(ex);
		}
		return true;
	}
	private String getName(Connection con, int itemID) throws XmlException {
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query
					+ " WHERE (mapd.typeID = 5 OR mapd.typeID = 3 OR mapd.groupID = 15)" //3 = Region 5 = Solar System
					+ " AND mapd.itemID = "+itemID+" "
					+ " ORDER BY mapd.itemID");
			if (rs == null) {
				return "";
			}
			while (rs.next()) {
				return rs.getString("itemName");
			}
		} catch (SQLException ex) {
			throw new XmlException(ex);
		}
		return "";
	}

	private String roundSecurity(double number) {
		if (number < 0) number = 0;
		number = number * 10;
		number = Math.round(number);
		number = number / 10;
		return securityformater.format(number);
	}

	@Override
	public String getName() {
		return "Locations";
	}
}
