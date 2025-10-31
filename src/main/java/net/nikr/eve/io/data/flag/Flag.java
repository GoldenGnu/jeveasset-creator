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

package net.nikr.eve.io.data.flag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Flag {
	private int flagID;
	private String flagName;
	private String flagText;

	public int getFlagID() {
		return flagID;
	}

	public String getFlagName() {
		return flagName;
	}

	public String getFlagText() {
		return flagText;
	}

	public void setFlagID(int flagID) {
		this.flagID = flagID;
	}

	public void setFlagName(String flagName) {
		this.flagName = flagName;
	}

	public void setFlagText(String flagText) {
		this.flagText = flagText;
	}
}
