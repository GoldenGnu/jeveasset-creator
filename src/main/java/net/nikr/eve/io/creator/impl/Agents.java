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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.Name;
import net.nikr.eve.io.data.agents.Agent;
import net.nikr.eve.io.data.agents.NpcCharacter;
import net.nikr.eve.io.esi.EsiUpdater;
import net.nikr.eve.io.esi.EsiUpdater.UpdateName;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.AgentReader;
import net.nikr.eve.util.Duration;
import net.troja.eve.esi.model.UniverseNamesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class Agents extends AbstractXmlWriter implements Creator {

	private final static Logger LOG = LoggerFactory.getLogger(Agents.class);

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
			LOG.error("Agents not saved (XML): "+ex.getMessage(), ex);
		}
		duration.end();
		LOG.info("	Agents done in " + duration.getString());
		return success;
	}

	@Override
	public String getName() {
		return "agents.xml";
	}

	@Override
	public File getFile() {
		return Program.getDataFile("agents.xml");
	}

	private boolean createFlags(Document xmldoc) throws XmlException {
		try {
			LOG.info("	YAML: Loading...");
			
			LOG.info("		Agents...");
			AgentReader agentsReader = new AgentReader();
			Map<Integer, NpcCharacter> agents = agentsReader.loadAgents();
			LOG.info("	XML: Creating...");
			Element parentNode = xmldoc.getDocumentElement();
			for (Map.Entry<Integer, NpcCharacter> entry : agents.entrySet()) {
				Element node = xmldoc.createElement("row");
				int agentID = entry.getKey();
				NpcCharacter npcCharacter = entry.getValue();
				Agent agent = npcCharacter.getAgent();
				if (agent == null) {
					continue;
				}
				if (npcCharacter.getEnglishName() == null) {
					System.out.println("OH NO!");
					continue;
				}
				int corporationID = npcCharacter.getCorporationID();
				node.setAttribute("agent", npcCharacter.getEnglishName());
				node.setAttribute("agentid", String.valueOf(agentID));
				node.setAttribute("corporationid", String.valueOf(corporationID));
				node.setAttribute("level", String.valueOf(agent.getLevel()));
				node.setAttribute("divisionid", String.valueOf(agent.getDivisionID()));
				node.setAttribute("agenttypeid", String.valueOf(agent.getAgentTypeID()));
				node.setAttribute("locationid", String.valueOf(npcCharacter.getLocationID()));
				node.setAttribute("locator", String.valueOf(agent.isIsLocator()));
				parentNode.appendChild(node);
			}
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return false;
	}

	private String getName(Map<Integer, Name> names, int id) {
		Name name = names.get(id);
		String entryName;
		if (name != null) {
			entryName = name.getItemName();
		} else {
			entryName = updateName(id);
			if (entryName == null) {
				throw new RuntimeException("name is null for ID: " + id);
			}
		}
		return entryName;
	}

	private String updateName(int id) {
		List<UpdateName> updates = new ArrayList<>();
		updates.add(new UpdateName(id));
		List<EsiUpdater.UpdateValues<List<UniverseNamesResponse>, Integer>> responses = EsiUpdater.updateValues(updates);
		for (EsiUpdater.UpdateValues<List<UniverseNamesResponse>, Integer> response : responses) {
			if (response.getResponse().isEmpty()) {
				return null;
			}
			return response.getResponse().get(0).getName();
		}
		return null;
	}
}
