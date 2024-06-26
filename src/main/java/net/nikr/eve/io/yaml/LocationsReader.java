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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.nikr.eve.io.data.map.Constellation;
import net.nikr.eve.io.data.map.LocationID;
import net.nikr.eve.io.data.map.Planet;
import net.nikr.eve.io.data.map.Region;
import net.nikr.eve.io.data.map.SolarSystem;




public class LocationsReader extends SolarSystemReader{

	public List<LocationID> loadLocations() throws IOException {
		List<LocationID> locations = Collections.synchronizedList(new ArrayList<>());
		List<SystemReader> systemReaders = Collections.synchronizedList(new ArrayList<>());
		processPaths(systemReaders, locations, 0, 0, Paths.get(YamlHelper.getFile(YamlHelper.SdeFile.UNIVERSE)));
		List<Future<Object>> futures = startReturn(systemReaders);
		for (Future<Object> reader : futures) {
			try {
				reader.get();
			} catch (InterruptedException | ExecutionException ex) {
				throw new RuntimeException(ex);
			}
		}
		return locations;
	}

	private void processPaths(List<SystemReader> systemReaders, List<LocationID> locations, int constellationID, int regionID, Path dir) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
		List<Path> directories = new ArrayList<>();
		for (Path path : stream) {
			if (path.toFile().isDirectory()) {
				directories.add(path); //Process subdirectories last (To make sure regionID is set)
			} else {
				if (path.getFileName().toString().equals(REGION)) {
					regionID = loadRegion(path.toAbsolutePath().toString());
					locations.add(new LocationID(0, 0, 0, regionID, 0));
				}
				if (path.getFileName().toString().equals(CONSTELLATION)) {
					constellationID = loadConstellation(path.toAbsolutePath().toString());
					locations.add(new LocationID(0, 0, constellationID, regionID, 0));
				}
				if (path.getFileName().toString().equals(SYSTEM)) {
					SystemReader reader = new SystemReader(locations, constellationID, regionID, path.toAbsolutePath().toString());
					systemReaders.add(reader);
				}
			}
		}
		for (Path path : directories) {
			processPaths(systemReaders, locations, constellationID, regionID, path);
		}
	}

	private void loadSolarSystem(List<LocationID> locationIDs, int constellationID, int regionID, String fullFilename) throws IOException {
		SolarSystem system = loadSolarSystem(fullFilename);
		int systemID = system.getSolarSystemID();
		float security = system.getSecurity();
		//System
		locationIDs.add(new LocationID(0, systemID, constellationID, regionID, security));
		//Stations
		for (Planet planet : system.getPlanets().values()) {
			for (String station : planet.getNpcStations().keySet()) {
				int stationID = Integer.parseInt(station);
				locationIDs.add(new LocationID(stationID, systemID, constellationID, regionID, security));
			}
			for (Planet moon : planet.getMoons().values()) {
				for (String station : moon.getNpcStations().keySet()) {
					int stationID = Integer.parseInt(station);
					locationIDs.add(new LocationID(stationID, systemID, constellationID, regionID, security));
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

	private int loadRegion(String filename) throws IOException {
		Region region = YamlHelper.read(filename, Region.class);
		return region.getRegionID();
	}

	private int loadConstellation(String filename) throws IOException {
		Constellation constellation = YamlHelper.read(filename, Constellation.class);
		return constellation.getConstellationID();
	}

	private class SystemReader implements Callable<Object> {

		private final List<LocationID> locations;
		private final int constellationID;
		private final int regionID;
		private final String fullFilename;

		public SystemReader(List<LocationID> locations, int constellationID, int regionID, String fullFilename) {
			this.locations = locations;
			this.constellationID = constellationID;
			this.regionID = regionID;
			this.fullFilename = fullFilename;
		}

		@Override
		public Object call() throws Exception {
			loadSolarSystem(locations, constellationID, regionID, fullFilename);
			return null;
		}
	}
}
