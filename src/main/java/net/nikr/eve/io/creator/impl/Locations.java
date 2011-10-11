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

	@Override
	public boolean create(File f, Connection con) {
		return saveLocations(con);
	}

	public boolean saveLocations(Connection con) {
		LOG.info("Locations:");
		Document xmldoc = null;
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
		Statement stmt = null;
		String query = "";
		ResultSet rs = null;
		Element parentNode = xmldoc.getDocumentElement();
		try {
			stmt = con.createStatement();
			query = "SELECT"
			+ "  mapd.itemID"
			+ ", mapd.typeID"
			//+ ", if (mapd.solarSystemID IS NULL, 0, mapd.solarSystemID) AS solarSystemID"
			+ ", CASE WHEN mapd.solarSystemID IS NULL THEN 0 ELSE mapd.solarSystemID END AS solarSystemID"
			//+ ", IF (mapSS.security IS NULL, mapd.security, mapSS.security) AS security"
			+ ", CASE WHEN mapSS.security IS NULL THEN mapd.security ELSE mapSS.security END AS security "
			+ ", mapd.regionID"
			+ ", mapd.itemName "
			+ " FROM mapDenormalize as mapd"
			+ " LEFT JOIN mapSolarSystems AS mapSS ON mapd.itemID = mapSS.solarSystemID"
			+ " WHERE mapd.typeID = 5 OR mapd.typeID = 3 OR mapd.groupID = 15" //3 = Region 5 = Solar System
			+ " ORDER BY mapd.itemID";
			rs = stmt.executeQuery(query);
			if (rs == null) return false;
			while (rs.next()) {
				Element node = xmldoc.createElementNS(null, "row");
				int id = rs.getInt("itemID");
				int typeID = rs.getInt("typeID");
				int ssid = 0;
				if (typeID == 5){ //If Solar System, use the itemID
					ssid = rs.getInt("itemID");
				} else {
					ssid = rs.getInt("solarSystemID");
				}
				node.setAttributeNS(null, "id", String.valueOf(id));
				node.setAttributeNS(null, "name", String.valueOf(rs.getString("itemName")));
				node.setAttributeNS(null, "region", String.valueOf(rs.getInt("regionID")));
				node.setAttributeNS(null, "solarsystem", String.valueOf(ssid));
				double security = 0;
				if (typeID == 5) { //System
					security = rs.getDouble("security");
				} else { //Region or Station (Region don't have security AKA 0.0)
					security = rs.getFloat("security");
				}
				node.setAttributeNS(null, "security", roundSecurity(security));
				parentNode.appendChild(node);
			}
		} catch (SQLException ex) {
			throw new XmlException(ex);
		}
		return true;
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
