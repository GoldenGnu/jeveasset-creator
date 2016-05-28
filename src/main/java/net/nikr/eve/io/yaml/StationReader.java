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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.nikr.eve.io.data.map.Station;
import net.nikr.eve.io.yaml.YamlHelper.SdeFile;


public class StationReader {
	public Map<Integer, Station> loadStations() throws IOException {
		YamlReader reader = YamlHelper.getReader(SdeFile.STASTATIONS);
		List<Station> list = reader.read(StationList.class, Station.class);
		Map<Integer, Station> map = new HashMap<Integer, Station>();
		for (Station name : list) {
			map.put(name.getStationID(), name);
		}
		return map;
	}

	public static class StationList extends ArrayList<Station> { }
	
}
