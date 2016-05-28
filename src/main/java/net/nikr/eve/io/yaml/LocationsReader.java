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

package net.nikr.eve.io.yaml;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.nikr.eve.io.data.map.LocationID;
import net.nikr.eve.io.data.map.Planet;
import net.nikr.eve.io.data.map.Region;
import net.nikr.eve.io.data.map.SolarSystem;
import org.slf4j.LoggerFactory;




public class LocationsReader extends SolarSystemReader{

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(JumpsReader.class);

	public List<LocationID> loadLocations() throws IOException {
		List<LocationID> locations = new ArrayList<LocationID>();
		process(locations, 0, Paths.get(YamlHelper.getFile(YamlHelper.SdeFile.UNIVERSE)));
		return locations;
	}

	private void process(List<LocationID> locations, int regionID, Path dir) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
		List<Path> directories = new ArrayList<Path>();
		for (Path path : stream) {
			if (path.toFile().isDirectory()) {
				directories.add(path); //Process subdirectories last (To make sure regionID is set)
			} else {
				if (path.getFileName().toString().equals("region.staticdata")) {
					regionID = loadRegion(path.toAbsolutePath().toString());
					locations.add(new LocationID(0, 0, regionID, 0));
				}
				if (path.getFileName().toString().equals("solarsystem.staticdata")) {
					loadSolarSystem(locations, regionID, path.toAbsolutePath().toString());
				}
			}
		}
		for (Path path : directories) {
			process(locations, regionID, path);
		}
	}

	private void loadSolarSystem(List<LocationID> locationIDs, int regionID, String fullFilename) throws IOException {
		SolarSystem system = loadSolarSystem(fullFilename);
		int systemID = system.getSolarSystemID();
		float security = system.getSecurity();
		//System
		locationIDs.add(new LocationID(0, systemID, regionID, security));
		//Stations
		for (Planet planet : system.getPlanets().values()) {
			for (String station : planet.getNpcStations().keySet()) {
				int stationID = Integer.valueOf(station);
				locationIDs.add(new LocationID(stationID, systemID, regionID, security));
			}
			for (Planet moon : planet.getMoons().values()) {
				for (String station : moon.getNpcStations().keySet()) {
					int stationID = Integer.valueOf(station);
					locationIDs.add(new LocationID(stationID, systemID, regionID, security));
				}
			}
		}
	}

	private int loadRegion(String filename) throws IOException {
		YamlReader reader = YamlHelper.getReader(filename);
		Region region = reader.read(Region.class);
		return region.getRegionID();
	}
}
