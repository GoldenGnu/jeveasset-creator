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
package net.nikr.eve.io.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Map;
import net.nikr.eve.io.data.agents.NpcCharacter;
import net.nikr.eve.io.data.npccorporations.Faction;
import net.nikr.eve.io.data.npccorporations.NpcCorporation;


public class AgentReader {
	public Map<Integer, NpcCharacter> loadAgents() throws IOException {
		return YamlHelper.read(YamlHelper.SdeFile.AGENTS, new TypeReference<Map<Integer, NpcCharacter>>(){});
	}

	public Map<Integer, NpcCorporation> loadNpcCorporations() throws IOException {
		return YamlHelper.read(YamlHelper.SdeFile.NPCCORPORATIONS, new TypeReference<Map<Integer, NpcCorporation>>(){});
	}

	public Map<Integer, Faction> loadFactions() throws IOException {
		return YamlHelper.read(YamlHelper.SdeFile.FACTIONS, new TypeReference<Map<Integer, Faction>>(){});
	}
}
