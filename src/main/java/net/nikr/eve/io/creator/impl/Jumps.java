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

public class Jumps extends AbstractXmlWriter implements Creator {

	@Override
	public void create(File f, Connection con) {
		saveJumps(con);
	}

	public boolean saveJumps(Connection con) {
		Log.info("Jumps:");
		Document xmldoc = null;
		boolean success = false;
		try {
			xmldoc = getXmlDocument("rows");
			Log.info("	Creating...");
			success = createLocations(xmldoc, con);
			Log.info("	Saving...");
			writeXmlFile(xmldoc, Program.getFilename("jumps.xml"));
		} catch (XmlException ex) {
			Log.error("Jumps not saved (XML): " + ex.getMessage(), ex);
		}
		Log.info("	Jumps done");
		return success;
	}

	private boolean createLocations(Document xmldoc, Connection con) throws XmlException {
		Statement stmt = null;
		String query = "";
		ResultSet rs = null;
		Element parentNode = xmldoc.getDocumentElement();
		try {
			stmt = con.createStatement();
			query = "select fromSolarSystemID, toSolarSystemID from mapSolarSystemJumps";
			rs = stmt.executeQuery(query);
			if (rs == null) return false;
			while (rs.next()) {
				Element node = xmldoc.createElementNS(null, "row");
				long from = rs.getLong("fromSolarSystemID");
				long to = rs.getLong("toSolarSystemID");
				node.setAttributeNS(null, "from", String.valueOf(from));
				node.setAttributeNS(null, "to", String.valueOf(to));
				parentNode.appendChild(node);
			}
		} catch (SQLException ex) {
			throw new XmlException(ex);
		}
		return true;
	}

	@Override
	public String getName() {
		return "Jumps";
	}
}
