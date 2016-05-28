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


public class Station {
	private int constellationID;
	private int corporationID;
	private int dockingCostPerVolume;
	private int maxShipVolumeDockable;
	private int officeRentalCost;
	private int operationID;
	private int regionID;
	private float reprocessingEfficiency;
	private int reprocessingHangarFlag;
	private float reprocessingStationsTake;
	private float security;
	private int solarSystemID;
	private int stationID;
	private String  stationName;
	private int stationTypeID;
	private float x;
	private float y;
	private float z;

	public int getStationID() {
		return stationID;
	}

	public String getStationName() {
		return stationName;
	}
}
