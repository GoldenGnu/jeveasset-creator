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

package net.nikr.eve.io.data.map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SolarSystem {
	private Map<String, Planet> planets;
	private float security;
	private int solarSystemID;
	private Map<String, Stargate> stargates;
	private List<Integer> stargateIDs;
	private Map<String, String> name;
	private int constellationID;
	private int regionID;

	public Map<String, Planet> getPlanets() {
		return planets;
	}

	public float getSecurity() {
		return security;
	}

	public int getSolarSystemID() {
		return solarSystemID;
	}

	public Map<String, Stargate> getStargates() {
		return stargates;
	}

	public List<Integer> getStargateIDs() {
		return stargateIDs;
	}

	public void setStargateIDs(List<Integer> stargateIDs) {
		this.stargateIDs = stargateIDs;
	}

	public Map<String, String> getName() {
		return name;
	}

	public String getEnglishName() {
		return name != null ? name.get("en") : null;
	}

	public int getConstellationID() {
		return constellationID;
	}

	public int getRegionID() {
		return regionID;
	}
}
