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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.nikr.eve.io.data.map.Jump;
import net.nikr.eve.io.data.map.SolarSystem;
import net.nikr.eve.io.data.map.Stargate;


public class JumpsReader extends SolarSystemReader {

	public Set<Jump> loadJumps() throws IOException {
		Map<Integer, Integer> destinationLookup = Collections.synchronizedMap(new HashMap<>());
		Set<Jump> destinationJumps = Collections.synchronizedSet(new TreeSet<>());
		List<JumpReader> jumpReaders = Collections.synchronizedList(new ArrayList<>());
		loadJumps(jumpReaders, destinationLookup, destinationJumps, Paths.get(YamlHelper.getFile(YamlHelper.SdeFile.UNIVERSE)));
		List<Future<Object>> futures = startReturn(jumpReaders);
		for (Future<Object> reader : futures) {
			try {
				reader.get();
			} catch (InterruptedException | ExecutionException ex) {
				throw new RuntimeException();
			}
		}
		Set<Jump> jumps = new TreeSet<>();
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
				if (path.getFileName().toString().equals(SYSTEM)) {
					JumpReader reader = new JumpReader(destinationLookup, destinationJumps, path.toAbsolutePath().toString());
					jumpReaders.add(reader);
				}
			}
		}
	}

	private void loadJumps(Map<Integer, Integer> destinationLookup, Set<Jump> destinationJumps, String fullFilename) throws IOException {
		SolarSystem system = loadSolarSystem(fullFilename);
		int systemID = system.getSolarSystemID();
		for (Map.Entry<String, Stargate> entry : system.getStargates().entrySet()) {
			int destinationFrom = Integer.parseInt(entry.getKey());
			int destinationTo = entry.getValue().getDestination();
			destinationLookup.put(destinationFrom, systemID);
			destinationJumps.add(new Jump(destinationFrom, destinationTo));
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

	private class JumpReader implements Callable<Object> {

		private final Map<Integer, Integer> destinationLookup;
		private final Set<Jump> destinationJumps;
		private final String fullFilename;

		public JumpReader(Map<Integer, Integer> destinationLookup, Set<Jump> destinationJumps, String fullFilename) {
			this.destinationLookup = destinationLookup;
			this.destinationJumps = destinationJumps;
			this.fullFilename = fullFilename;
		}

		@Override
		public Object call() throws Exception {
			loadJumps(destinationLookup, destinationJumps, fullFilename);
			return null;
		}
	}
}
