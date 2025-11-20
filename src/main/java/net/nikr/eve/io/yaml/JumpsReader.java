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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.nikr.eve.io.data.map.Jump;
import net.nikr.eve.io.data.map.SolarSystem;
import net.nikr.eve.io.data.map.Stargate;

public class JumpsReader extends SolarSystemReader {

	public Set<Jump> loadJumps() throws IOException {
		Map<Integer, Integer> stargateToSystem = Collections.synchronizedMap(new HashMap<>());
		Set<Jump> destinationJumps = Collections.synchronizedSet(new TreeSet<>());

		// Load stargates first to build stargate-to-system mapping
		Map<Integer, Stargate> stargates = YamlHelper.read(YamlHelper.SdeFile.STARGATES, new TypeReference<Map<Integer, Stargate>>() {});
		for (Map.Entry<Integer, Stargate> entry : stargates.entrySet()) {
			int stargateID = entry.getKey();
			Stargate stargate = entry.getValue();
			if (stargate.getSolarSystemID() != null) {
				stargateToSystem.put(stargateID, stargate.getSolarSystemID());
			}
		}

		Map<Integer, SolarSystem> systems = YamlHelper.read(YamlHelper.SdeFile.SYSTEMS, new TypeReference<Map<Integer, SolarSystem>>() {});
		for (SolarSystem system : systems.values()) {
			if (system.getStargateIDs() != null) {
				for (Integer stargateID : system.getStargateIDs()) {
					Stargate stargate = stargates.get(stargateID);
					if (stargate != null && stargate.getDestination() != null) {
						Integer destinationStargateID = stargate.getDestination().getStargateID();
						if (destinationStargateID != null) {
							destinationJumps.add(new Jump(stargateID, destinationStargateID));
						}
					}
				}
			}
		}

		Set<Jump> jumps = new TreeSet<>();
		for (Jump destinationJump : destinationJumps) {
			Integer fromSystem = stargateToSystem.get(destinationJump.getFrom());
			Integer toSystem = stargateToSystem.get(destinationJump.getTo());
			if (fromSystem != null && toSystem != null) {
				jumps.add(new Jump(fromSystem, toSystem));
			}
		}
		return jumps;
	}
}
