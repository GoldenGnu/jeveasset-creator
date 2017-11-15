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

package net.nikr.eve.io.creator;

import net.nikr.eve.io.creator.impl.sql.FlagsSql;
import net.nikr.eve.io.creator.impl.sql.ItemsSql;
import net.nikr.eve.io.creator.impl.sql.JumpsSql;
import net.nikr.eve.io.creator.impl.sql.LocationsSql;
/**
 *
 * @author Andrew Wheat
 */
public enum SqlCreators {
	LOCATIONS(new LocationsSql())
	, ITEMS(new ItemsSql())
	, JUMPS(new JumpsSql())
	, FLAGS(new FlagsSql())
	;

	Creator creator;

	private SqlCreators(Creator creator) {
		this.creator = creator;
	}

	public Creator getCreator() {
		return creator;
	}
}