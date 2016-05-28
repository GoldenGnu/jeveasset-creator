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

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SolarSystem {

	private boolean border;
	private List<Double> center;
	private boolean corridor;
	private List<Integer> disallowedAnchorCategories;
	private boolean fringe;
	private boolean hub;
	private boolean international;
	private float luminosity;
	private List<Double> max;
	private List<Double> min;
	public Map<String, Planet> planets = new HashMap<String, Planet>();
	private float radius;
	private boolean regional;
	private float security;
	private String securityClass;
	private int solarSystemID;
	private int solarSystemNameID;
	public Star star;
	public Star secondarySun;
	public Map<String, Stargate> stargates;
	private int sunTypeID;
	private int wormholeClassID;
	private int descriptionID;
	private int factionID;
	//
	private String visualEffect;
	private List<Integer> disallowedAnchorGroups;

	public int getSolarSystemID() {
		return solarSystemID;
	}

	public float getSecurity() {
		return security;
	}

	public Map<String, Planet> getPlanets() {
		return planets;
	}
}
