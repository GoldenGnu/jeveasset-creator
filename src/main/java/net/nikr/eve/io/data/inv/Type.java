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

package net.nikr.eve.io.data.inv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Type {

	private double volume; //invTypes.volume
	private Map<String, String> name; //invTypes.typeName
	private double basePrice; //invTypes.basePrice
	private long marketGroupID; //invTypes.marketGroupID
	private long portionSize; //invTypes.portionSize
	private boolean published; //invTypes.published
	private Long metaGroupID; //invTypes.metaGroupID
	private long groupID;
	private double capacity;

	public double getVolume() {
		return volume;
	}

	public Map<String, String> getName() {
		return name;
	}

	public double getBasePrice() {
		return basePrice;
	}

	public long getMarketGroupID() {
		return marketGroupID;
	}

	public long getPortionSize() {
		return portionSize;
	}

	public boolean isPublished() {
		return published;
	}

	public Long getMetaGroupID() {
		return metaGroupID;
	}

	public long getGroupID() {
		return groupID;
	}

	public double getCapacity() {
		return capacity;
	}

	public String getEnglishName() {
		return name.get("en");
	}

	public void setEnglishName(String englishName) {
		name.put("en", englishName);
	}

	@Override
	public String toString() {
		return getEnglishName();
	}
}
