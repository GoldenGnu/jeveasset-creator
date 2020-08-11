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

package net.nikr.eve.io.data.inv;

import java.util.List;
import java.util.Map;


public class Type {

	private double volume; //invTypes.volume
	private Map<String, String> name; //invTypes.typeName
	private double basePrice; //invTypes.basePrice
	private int marketGroupID; //invTypes.marketGroupID
	private int portionSize; //invTypes.portionSize
	private boolean published; //invTypes.published
	private Integer metaGroupID; //invTypes.metaGroupID
	//Unused
	private int groupID;
	private double mass;
	private double radius;
	private int graphicID;
	private int soundID;
	private Map<String, String> description;
	private int iconID;
	private int raceID;
	private double capacity;
	private String sofFactionName;
	private int factionID;
	private Map<Integer, List<Integer>> masteries;
	private Map<Integer, List<Integer>> traits;
	private String sofDnaAddition;
	private int sofMaterialSetID;
	private int variationParentTypeID;

	public Type() { }

	public int getMarketGroupID() {
		return marketGroupID;
	}

	public Integer getMetaGroupID() {
		return metaGroupID;
	}

	public int getGroupID() {
		return groupID;
	}

	public boolean isPublished() {
		return published;
	}

	public String getName() {
		return name.get("en");
	}

	public double getBasePrice() {
		return basePrice;
	}

	public double getVolume() {
		return volume;
	}

	public double getCapacity() {
		return capacity;
	}

	public int getPortionSize() {
		return portionSize;
	}

	@Override
	public String toString() {
		return getName();
	}
}
