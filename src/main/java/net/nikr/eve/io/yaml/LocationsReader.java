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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.nikr.eve.io.data.map.Constellation;
import net.nikr.eve.io.data.map.LocationID;
import net.nikr.eve.io.data.map.NpcStation;
import net.nikr.eve.io.data.map.Planet;
import net.nikr.eve.io.data.map.Region;
import net.nikr.eve.io.data.map.SolarSystem;

public class LocationsReader extends SolarSystemReader {

	public List<LocationID> loadLocations() throws IOException {
		List<LocationID> locations = Collections.synchronizedList(new ArrayList<>());

		Map<Integer, Region> regions = YamlHelper.read(YamlHelper.SdeFile.REGIONS, new TypeReference<Map<Integer, Region>>() {});
		for (Map.Entry<Integer, Region> entry : regions.entrySet()) {
			locations.add(new LocationID(0, 0, 0, entry.getKey(), 0));
		}

		Map<Integer, Constellation> constellations = YamlHelper.read(YamlHelper.SdeFile.CONSTELLATIONS, new TypeReference<Map<Integer, Constellation>>() {});
		for (Map.Entry<Integer, Constellation> entry : constellations.entrySet()) {
			locations.add(new LocationID(0, 0, entry.getKey(), entry.getValue().getRegionID(), 0));
		}

		Map<Integer, SolarSystem> systems = YamlHelper.read(YamlHelper.SdeFile.SYSTEMS, new TypeReference<Map<Integer, SolarSystem>>() {});
		List<SystemReader> systemReaders = Collections.synchronizedList(new ArrayList<>());
		for (Map.Entry<Integer, SolarSystem> entry : systems.entrySet()) {
			int systemID = entry.getKey();
			SolarSystem system = entry.getValue();
			int constellationID = system.getConstellationID();
			int regionID = system.getRegionID();
			SystemReader reader = new SystemReader(locations, systemID, constellationID, regionID, system);
			systemReaders.add(reader);
		}
		List<Future<Object>> futures = startReturn(systemReaders);
		for (Future<Object> reader : futures) {
			try {
				reader.get();
			} catch (InterruptedException | ExecutionException ex) {
				throw new RuntimeException(ex);
			}
		}

		Map<Integer, Integer> systemToConstellation = new HashMap<>();
		Map<Integer, Integer> systemToRegion = new HashMap<>();
		for (Map.Entry<Integer, SolarSystem> entry : systems.entrySet()) {
			int systemID = entry.getKey();
			SolarSystem system = entry.getValue();
			systemToConstellation.put(systemID, system.getConstellationID());
			systemToRegion.put(systemID, system.getRegionID());
		}

		Map<Integer, NpcStation> npcStations = YamlHelper.read(YamlHelper.SdeFile.NPCSTATIONS, new TypeReference<Map<Integer, NpcStation>>() {});
		for (Map.Entry<Integer, NpcStation> entry : npcStations.entrySet()) {
			int stationID = entry.getKey();
			NpcStation station = entry.getValue();
			Integer solarSystemID = station.getSolarSystemID();
			if (solarSystemID != null) {
				SolarSystem system = systems.get(solarSystemID);
				if (system == null) {
					continue;
				}
				Integer constellationID = systemToConstellation.get(solarSystemID);
				Integer regionID = systemToRegion.get(solarSystemID);
				if (constellationID != null && regionID != null) {
					float security = system.getSecurityStatus();
					locations.add(new LocationID(stationID, solarSystemID, constellationID, regionID, security));
				}
			}
		}

		return locations;
	}

	private void loadSolarSystem(List<LocationID> locationIDs, int systemID, int constellationID, int regionID,
			SolarSystem system) throws IOException {
		if (system == null) {
			return;
		}
		float security = system.getSecurityStatus();
		// System
		locationIDs.add(new LocationID(0, systemID, constellationID, regionID, security));
		// Stations
		if (system.getPlanets() != null) {
			for (Planet planet : system.getPlanets().values()) {
				if (planet.getNpcStations() != null) {
					for (String station : planet.getNpcStations().keySet()) {
						int stationID = Integer.parseInt(station);
						locationIDs.add(new LocationID(stationID, systemID, constellationID, regionID, security));
					}
				}
				if (planet.getMoons() != null) {
					for (Planet moon : planet.getMoons().values()) {
						if (moon.getNpcStations() != null) {
							for (String station : moon.getNpcStations().keySet()) {
								int stationID = Integer.parseInt(station);
								locationIDs.add(new LocationID(stationID, systemID, constellationID, regionID, security));
							}
						}
					}
				}
			}
		}
	}

	public static <K> List<Future<K>> startReturn(Collection<? extends Callable<K>> updaters) {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		try {
			return executor.invokeAll(updaters);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private class SystemReader implements Callable<Object> {

		private final List<LocationID> locations;
		private final int systemID;
		private final int constellationID;
		private final int regionID;
		private final SolarSystem system;

		public SystemReader(List<LocationID> locations, int systemID, int constellationID, int regionID,
				SolarSystem system) {
			this.locations = locations;
			this.systemID = systemID;
			this.constellationID = constellationID;
			this.regionID = regionID;
			this.system = system;
		}

		@Override
		public Object call() throws Exception {
			loadSolarSystem(locations, systemID, constellationID, regionID, system);
			return null;
		}
	}
}
