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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import net.nikr.eve.io.data.map.Jump;
import net.nikr.eve.io.data.map.SolarSystem;
import net.nikr.eve.io.data.map.Stargate;
import org.slf4j.LoggerFactory;


public class JumpsReader extends SolarSystemReader {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(JumpReader.class);

	public Set<Jump> loadJumps() throws IOException {
		Map<Integer, Integer> destinationLookup = Collections.synchronizedMap(new HashMap<>());
		Set<Jump> destinationJumps = Collections.synchronizedSet(new TreeSet<Jump>());
		List<JumpReader> jumpReaders = Collections.synchronizedList(new ArrayList<JumpReader>());
		loadJumps(jumpReaders, destinationLookup, destinationJumps, Paths.get(YamlHelper.getFile(YamlHelper.SdeFile.UNIVERSE)));
		for (JumpReader reader : jumpReaders) {
			try {
				reader.get();
			} catch (InterruptedException | ExecutionException ex) {
				throw new RuntimeException();
			}
		}
		Set<Jump> jumps = new TreeSet<Jump>();
		for (Jump destinationJump : destinationJumps) {
			int from = destinationLookup.get(destinationJump.getFrom());
			int to = destinationLookup.get(destinationJump.getTo());
			jumps.add(new Jump(from, to));
		}
		return jumps;
	}

	private void loadJumps(List<JumpReader> jumpReaders, Map<Integer, Integer> destinationLookup, Set<Jump> destinationJumps, Path dir) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
		for (Path path : stream) {
			if (path.toFile().isDirectory()) {
				loadJumps(jumpReaders, destinationLookup, destinationJumps, path);
			} else {
				if (path.getFileName().toString().equals("solarsystem.staticdata")) {
					JumpReader reader = new JumpReader(destinationLookup, destinationJumps, path.toAbsolutePath().toString());
					jumpReaders.add(reader);
					reader.execute();
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

	private class JumpReader extends SwingWorker<Void, Void>{

		private final Map<Integer, Integer> destinationLookup;
		private final Set<Jump> destinationJumps;
		private final String fullFilename;

		public JumpReader(Map<Integer, Integer> destinationLookup, Set<Jump> destinationJumps, String fullFilename) {
			this.destinationLookup = destinationLookup;
			this.destinationJumps = destinationJumps;
			this.fullFilename = fullFilename;
		}

		@Override
		protected Void doInBackground() throws Exception {
			loadJumps(destinationLookup, destinationJumps, fullFilename);
			return null;
		}
	}
}
