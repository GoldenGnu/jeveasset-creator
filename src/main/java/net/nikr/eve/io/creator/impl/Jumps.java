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

package net.nikr.eve.io.creator.impl;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.map.Jump;
import net.nikr.eve.util.Duration;
import net.nikr.eve.io.yaml.JumpsReader;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Jumps extends AbstractXmlWriter implements Creator {
	
	private final static Logger LOG = LoggerFactory.getLogger(Jumps.class);

	@Override
	public boolean create() {
		Duration duration = new Duration();
		duration.start();
		LOG.info("Jumps:");
		boolean success = false;
		try {
			Document xmldoc = getXmlDocument("rows");
			LOG.info("	XML: init...");
			Comment comment = xmldoc.createComment("Generated from Eve Online Toolkit. ©CCP hf. All rights reserved. Used with permission.");
			xmldoc.getDocumentElement().appendChild(comment);
			success = createJumps(xmldoc);
			LOG.info("	XML: Saving...");
			writeXmlFile(xmldoc, getFile());
		} catch (XmlException ex) {
			LOG.error("Jumps not saved (XML): " + ex.getMessage(), ex);
		}
		duration.end();
		LOG.info("	Jumps done in " + duration.getString());
		return success;
	}

	@Override
	public String getName() {
		return "jumps.xml";
	}

	@Override
	public File getFile() {
		return Program.getDataFile("jumps.xml");
	}

	private boolean createJumps(Document xmldoc) throws XmlException {
		try {
			LOG.info("	YAML: Loading...");
			JumpsReader jumpsReader = new JumpsReader();
			Set<Jump> jumps = jumpsReader.loadJumps();
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			for (Jump jump : jumps) {
				Element node = xmldoc.createElementNS(null, "row");
				long from = jump.getFrom();
				long to = jump.getTo();
				node.setAttributeNS(null, "from", String.valueOf(from));
				node.setAttributeNS(null, "to", String.valueOf(to));
				parentNode.appendChild(node);
			}
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return false;
	}
}
