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
import java.sql.SQLException;
import javax.swing.JOptionPane;
import net.nikr.eve.Program;
import net.nikr.eve.io.AbstractXmlWriter;
import net.nikr.eve.io.XmlException;
import net.nikr.eve.io.creator.Creator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Version extends AbstractXmlWriter implements Creator {
	
	private final static Logger LOG = LoggerFactory.getLogger(Version.class);

	@Override
	public boolean create() {
		LOG.info("Version:");
		boolean success = false;
		try {
			Document xmldoc = getXmlDocument("rows");
			LOG.info("	Creating...");
			success = createVersion(xmldoc);
			if (success){
				LOG.info("	Saving...");
				writeXmlFile(xmldoc, Program.getFilename("data"+File.separator+"data.xml"));
			}
		} catch (XmlException ex) {
			LOG.error("Version not saved (XML): "+ex.getMessage(), ex);
		}
		LOG.info("	Version done");
		return success;
	}

	private boolean createVersion(Document xmldoc) throws XmlException {
		Element parentNode = xmldoc.getDocumentElement();
		String value = (String) JOptionPane.showInputDialog(null, "Enter version: [NAME] X.X.X.XXXXX", "Version", JOptionPane.QUESTION_MESSAGE, null, null, getDatabase());
		if (value != null){
			Element node = xmldoc.createElementNS(null, "row");
			node.setAttributeNS(null, "version", value);
			parentNode.appendChild(node);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getName() {
		return "Version";
	}

	private String getDatabase() {
		Connection connection = Program.openConnection();
		try {
			String value = connection.getMetaData().getURL();
			int start = value.lastIndexOf("/") + 1;
			if (start >= 0 && start <= value.length()) {
				return value.substring(start);
			} else {
				return value;
			}
		} catch (SQLException ex) {
			LOG.warn("Failed to get database name");
			return "";
		} finally {
			Program.close(connection);
		}
	}

}
