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
import net.nikr.eve.io.data.map.Constellation;
import net.nikr.eve.io.data.map.NpcStation;
import net.nikr.eve.io.data.map.Region;
import net.nikr.eve.io.data.map.SolarSystem;

public class LocationsReader extends SolarSystemReader {

	public Map<Integer, Region> loadRegions() throws IOException {
		return YamlHelper.read(YamlHelper.SdeFile.REGIONS, new TypeReference<Map<Integer, Region>>() {});
	}

	public Map<Integer, Constellation> loadConstellation() throws IOException {
		return YamlHelper.read(YamlHelper.SdeFile.CONSTELLATIONS, new TypeReference<Map<Integer, Constellation>>() {});
	}

	public Map<Integer, SolarSystem> loadSystems() throws IOException {
		return YamlHelper.read(YamlHelper.SdeFile.SYSTEMS, new TypeReference<Map<Integer, SolarSystem>>() {});
	}

	public Map<Integer, NpcStation> loadStations() throws IOException {
		return YamlHelper.read(YamlHelper.SdeFile.NPCSTATIONS, new TypeReference<Map<Integer, NpcStation>>() {});
	}
}
