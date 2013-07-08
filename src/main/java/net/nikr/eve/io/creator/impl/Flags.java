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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.nikr.eve.Program;
import net.nikr.eve.io.AbstractXmlWriter;
import net.nikr.eve.io.XmlException;
import net.nikr.eve.io.creator.Creator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Flags extends AbstractXmlWriter implements Creator {
	
	private final static Logger LOG = LoggerFactory.getLogger(Flags.class);

	@Override
	public boolean create() {
		LOG.info("Flags:");
		boolean success = false;
		try {
			Document xmldoc = getXmlDocument("rows");
			LOG.info("	Creating...");
			success = createFlags(xmldoc);
			LOG.info("	Saving...");
			writeXmlFile(xmldoc, Program.getFilename("data"+File.separator+"flags.xml"));
		} catch (XmlException ex) {
			LOG.error("Flags not saved (XML): "+ex.getMessage(), ex);
		}
		LOG.info("	Flags done");
		return success;
	}

	private boolean createFlags(Document xmldoc) throws XmlException {
		Element parentNode = xmldoc.getDocumentElement();
		ResultSet rs = null;
		Statement stmt = null;
		Connection connection = Program.openConnection();
		try {
			stmt = connection.createStatement();
			String query = "SELECT flagID, flagName, flagText FROM invFlags ORDER BY flagID" ;
			rs = stmt.executeQuery(query);
			if (rs == null) return false;
			while (rs.next()) {
				Element node = xmldoc.createElementNS(null, "row");
				int flagID = rs.getInt("flagID");
				String flagName = rs.getString("flagName");
				String flagText = rs.getString("flagText");
				node.setAttributeNS(null, "flagid", String.valueOf(flagID));
				node.setAttributeNS(null, "flagname", flagName);
				node.setAttributeNS(null, "flagtext", flagText);
				parentNode.appendChild(node);
			}
		} catch (SQLException ex) {
			throw new XmlException(ex);
		} finally {
			Program.close(rs);
			Program.close(stmt);
			Program.close(connection);
		}
		return true;
	}

	@Override
	public String getName() {
		return "Flags";
	}

}
