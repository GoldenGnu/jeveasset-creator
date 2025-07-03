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
import java.util.Map;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.npccorporations.Faction;
import net.nikr.eve.io.data.npccorporations.NpcCorporation;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.AgentReader;
import net.nikr.eve.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class NpcCorporations extends AbstractXmlWriter implements Creator {

	private final static Logger LOG = LoggerFactory.getLogger(NpcCorporation.class);

	@Override
	public boolean create() {
		Duration duration = new Duration();
		duration.start();
		LOG.info("Agents:");
		boolean success = false;
		try {
			Document xmldoc = getXmlDocument("rows");
			LOG.info("	XML: init...");
			Comment comment = xmldoc.createComment("Generated from Eve Online Toolkit. ©CCP hf. All rights reserved. Used with permission.");
			xmldoc.getDocumentElement().appendChild(comment);
			success = createFlags(xmldoc);
			LOG.info("	XML: Saving...");
			writeXmlFile(xmldoc, getFile());
		} catch (XmlException ex) {
			LOG.error("Npc Corporation not saved (XML): "+ex.getMessage(), ex);
		}
		duration.end();
		LOG.info("	Npc Corporation done in " + duration.getString());
		return success;
	}

	@Override
	public String getName() {
		return "npccorporation.xml";
	}

	@Override
	public File getFile() {
		return Program.getDataFile("npccorporation.xml");
	}

	private boolean createFlags(Document xmldoc) throws XmlException {
		try {
			LOG.info("	YAML: Loading...");
			AgentReader reader = new AgentReader();
			LOG.info("		NpcCorporations...");
			Map<Integer, NpcCorporation> npcCorporations = reader.loadNpcCorporations();
			LOG.info("		Factions...");
			Map<Integer, Faction> factions = reader.loadFactions();
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			for (Map.Entry<Integer, NpcCorporation> entry : npcCorporations.entrySet()) {
				Element node = xmldoc.createElement("row");
				int corporationID = entry.getKey();
				NpcCorporation corporation = entry.getValue();
				int factionID = corporation.getFactionID();
				if (factionID == 0 || factionID == 500021) {
					continue; //Doomheim, Polaris, etc.
				}
				String corporationName = corporation.getEnglishName();
				Faction faction = factions.get(factionID);
				String factionName = faction.getEnglishName();
				boolean criminalConnections = criminalConnections(factionName);
				boolean connections = connections(factionName);
				if (!criminalConnections && !connections) {
					throw new RuntimeException("No connections???");
				}
				node.setAttribute("factionid", String.valueOf(factionID));
				node.setAttribute("faction", factionName);
				node.setAttribute("corporation", corporationName);
				node.setAttribute("corporationid", String.valueOf(corporationID));
				if (connections) {
					node.setAttribute("c", String.valueOf(true));
				}
				if (criminalConnections) {
					node.setAttribute("cc", String.valueOf(true));
				}
				parentNode.appendChild(node);
			}
			for (Map.Entry<Integer, Faction> entry : factions.entrySet()) {
				Element node = xmldoc.createElement("row");
				int factionID = entry.getKey();
				if (factionID == 0 || factionID == 500021) {
					continue; //Doomheim, Polaris, etc.
				}
				Faction faction = entry.getValue();
				String factionName = faction.getEnglishName();
				boolean criminalConnections = criminalConnections(factionName);
				boolean connections = connections(factionName);
				if (!criminalConnections && !connections) {
					throw new RuntimeException("No connections???");
				}
				node.setAttribute("factionid", String.valueOf(factionID));
				if (connections) {
					node.setAttribute("c", String.valueOf(true));
				}
				if (criminalConnections) {
					node.setAttribute("cc", String.valueOf(true));
				}
				parentNode.appendChild(node);
			}
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return false;
	}

	private boolean criminalConnections(String factionName) {
		return factionName.equalsIgnoreCase("Guristas Pirates")
				|| factionName.equalsIgnoreCase("Angel Cartel")
				|| factionName.equalsIgnoreCase("Blood Raider Covenant")
				|| factionName.equalsIgnoreCase("Sansha's Nation")
				|| factionName.equalsIgnoreCase("Serpentis")
				|| factionName.equalsIgnoreCase("Deathless Circle");
	}

	private boolean connections(String factionName) {
		return factionName.equalsIgnoreCase("Amarr Empire")
				|| factionName.equalsIgnoreCase("The Society of Conscious Thought")
				|| factionName.equalsIgnoreCase("Drifters")
				|| factionName.equalsIgnoreCase("Gallente Federation")
				|| factionName.equalsIgnoreCase("Mordu's Legion Command")
				|| factionName.equalsIgnoreCase("Rogue Drones")
				|| factionName.equalsIgnoreCase("Jove Empire")
				|| factionName.equalsIgnoreCase("Triglavian Collective")
				|| factionName.equalsIgnoreCase("EverMore")
				|| factionName.equalsIgnoreCase("EDENCOM")
				|| factionName.equalsIgnoreCase("Ammatar Mandate")
				|| factionName.equalsIgnoreCase("ORE")
				|| factionName.equalsIgnoreCase("Association for Interdisciplinary Research")
				|| factionName.equalsIgnoreCase("Caldari State")
				|| factionName.equalsIgnoreCase("Khanid Kingdom")
				|| factionName.equalsIgnoreCase("Thukker Tribe")
				|| factionName.equalsIgnoreCase("Minmatar Republic")
				|| factionName.equalsIgnoreCase("The Syndicate")
				|| factionName.equalsIgnoreCase("Servant Sisters of EVE")
				|| factionName.equalsIgnoreCase("CONCORD Assembly");
	}
}
