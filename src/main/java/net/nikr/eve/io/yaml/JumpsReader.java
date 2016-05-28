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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.nikr.eve.io.data.map.Jump;
import net.nikr.eve.io.data.map.SolarSystem;
import net.nikr.eve.io.data.map.Stargate;
import org.slf4j.LoggerFactory;


public class JumpsReader extends SolarSystemReader {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(JumpsReader.class);

	public Set<Jump> loadJumps() throws IOException {
		Map<Integer, Integer> destinationLookup = new HashMap<>();
		Set<Jump> destinationJumps = new TreeSet<Jump>();
		loadJumps(destinationLookup, destinationJumps, Paths.get(YamlHelper.getFile(YamlHelper.SdeFile.UNIVERSE)));
		Set<Jump> jumps = new TreeSet<Jump>();
		for (Jump destinationJump : destinationJumps) {
			int from = destinationLookup.get(destinationJump.getFrom());
			int to = destinationLookup.get(destinationJump.getTo());
			jumps.add(new Jump(from, to));
		}
		return jumps;
	}
	private void loadJumps(Map<Integer, Integer> destinationLookup, Set<Jump> destinationJumps, Path dir) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
		for (Path path : stream) {
			if (path.toFile().isDirectory()) {
				loadJumps(destinationLookup, destinationJumps, path);
			} else {
				if (path.getFileName().toString().equals("solarsystem.staticdata")) {
					loadJumps(destinationLookup, destinationJumps, path.toAbsolutePath().toString());
				}
			}
		}
	}

	private void loadJumps(Map<Integer, Integer> destinationLookup, Set<Jump> destinationJumps, String fullFilename) throws IOException {
		SolarSystem system = loadSolarSystem(fullFilename);
		int systemID = system.getSolarSystemID();
		for (Map.Entry<String, Stargate> entry : system.stargates.entrySet()) {
			int destinationFrom = Integer.valueOf(entry.getKey());
			int destinationTo = entry.getValue().getDestination();
			destinationLookup.put(destinationFrom, systemID);
			destinationJumps.add(new Jump(destinationFrom, destinationTo));
		}
	}
}
