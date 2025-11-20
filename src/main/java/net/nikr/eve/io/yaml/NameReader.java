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
import java.util.HashMap;
import java.util.Map;
import net.nikr.eve.io.data.Name;
import net.nikr.eve.io.data.inv.Type;
import net.nikr.eve.io.data.map.Constellation;
import net.nikr.eve.io.data.map.NpcStation;
import net.nikr.eve.io.data.map.Region;
import net.nikr.eve.io.data.map.SolarSystem;
import net.nikr.eve.io.yaml.YamlHelper.SdeFile;
import net.troja.eve.esi.model.StationResponse;
import static net.nikr.eve.io.esi.EsiUpdater.DATASOURCE;
import static net.nikr.eve.io.esi.EsiUpdater.UNIVERSE_API;
import net.troja.eve.esi.ApiException;


public class NameReader {
	public Map<Integer, Name> loadNames() throws IOException {
		Map<Integer, Name> map = new HashMap<>();

		InvReader invReader = new InvReader();
		Map<Integer, Type> types = invReader.loadTypes();

		Map<Integer, Region> regions = YamlHelper.read(SdeFile.REGIONS, new TypeReference<Map<Integer, Region>>(){});
		for (Map.Entry<Integer, Region> entry : regions.entrySet()) {
			Region region = entry.getValue();
			String name = region.getEnglishName();
			if (name != null) {
				Name nameObj = new Name();
				nameObj.setItemID(entry.getKey());
				nameObj.setItemName(name);
				map.put(entry.getKey(), nameObj);
			}
		}

		Map<Integer, Constellation> constellations = YamlHelper.read(SdeFile.CONSTELLATIONS, new TypeReference<Map<Integer, Constellation>>(){});
		for (Map.Entry<Integer, Constellation> entry : constellations.entrySet()) {
			Constellation constellation = entry.getValue();
			String name = constellation.getEnglishName();
			if (name != null) {
				Name nameObj = new Name();
				nameObj.setItemID(entry.getKey());
				nameObj.setItemName(name);
				map.put(entry.getKey(), nameObj);
			}
		}

		Map<Integer, SolarSystem> systems = YamlHelper.read(SdeFile.SYSTEMS, new TypeReference<Map<Integer, SolarSystem>>(){});
		for (Map.Entry<Integer, SolarSystem> entry : systems.entrySet()) {
			SolarSystem system = entry.getValue();
			String name = system.getEnglishName();
			if (name != null) {
				Name nameObj = new Name();
				nameObj.setItemID(entry.getKey());
				nameObj.setItemName(name);
				map.put(entry.getKey(), nameObj);
			}
		}

		Map<Integer, NpcStation> stations = YamlHelper.read(SdeFile.NPCSTATIONS, new TypeReference<Map<Integer, NpcStation>>(){});
		for (Map.Entry<Integer, NpcStation> entry : stations.entrySet()) {
			Integer stationID = entry.getKey();
			try {
				// Direct ESI API call for live station data
				StationResponse response = UNIVERSE_API.getUniverseStationsStationId(stationID, DATASOURCE, null);

				if (response != null && response.getName() != null) {
					Name nameObj = new Name();
					nameObj.setItemID(stationID);
					nameObj.setItemName(response.getName());
					map.put(stationID, nameObj);
				}

			} catch (ApiException e) {
				System.err.printf("Failed to fetch station %d: %s%n", stationID, e.getMessage());
			}
		}

		return map;
	}
}
