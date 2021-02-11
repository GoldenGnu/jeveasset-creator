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

package net.nikr.eve.io.data.map;


public class Location implements Comparable<Location> {
	private final int locationID;
	private final int stationID;
	private final String stationName;
	private final int systemID;
	private final String systemName;
	private final int constellationID;
	private final String constellationName;
	private final int regionID;
	private final String regionName;
	private final float security;

	public Location(int stationID, String stationName, int systemID, String systemName, int constellationID, String constellationName, int regionID, String regionName, float security) {
		this.stationID = stationID;
		this.stationName = stationName;
		this.systemID = systemID;
		this.systemName = systemName;
		this.constellationID = constellationID;
		this.constellationName = constellationName;
		this.regionID = regionID;
		this.regionName = regionName;
		this.security = security;
		if (stationID != 0) {
			locationID = stationID;
		} else if (systemID != 0) {
			locationID = systemID;
		} else if (regionID != 0) {
			locationID = regionID;
		} else {
			throw new RuntimeException(this.toString());
		}
	}

	public int getStationID() {
		return stationID;
	}

	public String getStationName() {
		return stationName;
	}

	public int getSystemID() {
		return systemID;
	}

	public String getSystemName() {
		return systemName;
	}

	public int getConstellationID() {
		return constellationID;
	}

	public String getConstellationName() {
		return constellationName;
	}

	public int getRegionID() {
		return regionID;
	}

	public String getRegionName() {
		return regionName;
	}

	public float getSecurity() {
		return security;
	}

	public int getLocationID() {
		return locationID;
	}

	@Override
	public int compareTo(Location o) {
		return Integer.compare(locationID, o.locationID);
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + this.locationID;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Location other = (Location) obj;
		if (this.locationID != other.locationID) {
			return false;
		}
		return true;
	}
}
