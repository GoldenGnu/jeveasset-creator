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
import javax.swing.JOptionPane;
import net.nikr.eve.Program;
import net.nikr.eve.io.AbstractXmlWriter;
import net.nikr.eve.io.XmlException;
import net.nikr.eve.io.creator.Creator;
import net.nikr.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Version extends AbstractXmlWriter implements Creator {

	@Override
	public void create(File f, Connection con) {
		saveVersion(con);
	}

	public boolean saveVersion(Connection con){
		Log.info("Version:");
		Document xmldoc = null;
		boolean success = false;
		try {
			xmldoc = getXmlDocument("rows");
			Log.info("	Creating...");
			success = createVersion(xmldoc, con);
			if (success){
				Log.info("	Saving...");
				writeXmlFile(xmldoc, Program.getFilename("data.xml"));
			}
		} catch (XmlException ex) {
			Log.error("Version not saved (XML): "+ex.getMessage(), ex);
		}
		Log.info("	Version done");
		return success;
	}

	private boolean createVersion(Document xmldoc, Connection con) throws XmlException {
		Element parentNode = xmldoc.getDocumentElement();
		String value = JOptionPane.showInputDialog(null, "Enter version: [NAME] X.X.X.XXXXX", "Version", JOptionPane.QUESTION_MESSAGE);
		if (value != null){
			Element node = xmldoc.createElementNS(null, "row");
			node.setAttributeNS(null, "version", value);
			parentNode.appendChild(node);
			return true;
		} else {
			int i = JOptionPane.showConfirmDialog(null, "Cancel creation of version.xml?\r\nNo to retry...", "Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (i == JOptionPane.YES_OPTION){
				Log.info("	Creation cancelled...");
				return false;
			} else {
				return createVersion(xmldoc, con);
			}
		}
	}

	@Override
	public String getName() {
		return "Version";
	}

}
