/*
 * Copyright 2009-2023 Contributors (see credits.txt)
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
import java.util.List;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.flag.Flag;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.FlagsReader;
import net.nikr.eve.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Flags extends AbstractXmlWriter implements Creator {

	private final static Logger LOG = LoggerFactory.getLogger(Flags.class);

	@Override
	public boolean create() {
		Duration duration = new Duration();
		duration.start();
		LOG.info("Flags:");
		boolean success = false;
		try {
			Document xmldoc = getXmlDocument("rows");
			LOG.info("	XML: init...");
			Comment comment = xmldoc
					.createComment("Generated from Eve Online Toolkit. ©CCP hf. All rights reserved. Used with permission.");
			xmldoc.getDocumentElement().appendChild(comment);
			success = createFlags(xmldoc);
			LOG.info("	XML: Saving...");
			writeXmlFile(xmldoc, getFile());
		} catch (XmlException ex) {
			LOG.error("Flags not saved (XML): " + ex.getMessage(), ex);
		}
		duration.end();
		LOG.info("	Flags done in " + duration.getString());
		return success;
	}

	@Override
	public String getName() {
		return "flags.xml";
	}

	@Override
	public File getFile() {
		return Program.getDataFile("flags.xml");
	}

	private boolean createFlags(Document xmldoc) throws XmlException {
		try {
			LOG.info("	YAML: Loading...");
			FlagsReader reader = new FlagsReader();
			List<Flag> flags = reader.loadFlags();
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			for (Flag flag : flags) {
				Element node = xmldoc.createElement("row");
				int flagID = flag.getFlagID();
				String flagName = flag.getFlagName();
				String flagText = flag.getFlagText();
				node.setAttribute("flagid", String.valueOf(flagID));
				node.setAttribute("flagname", flagName);
				node.setAttribute("flagtext", flagText);
				parentNode.appendChild(node);
			}
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return false;
	}
}
