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
import net.nikr.eve.io.creator.impl.yaml.FlagsYaml;
import net.nikr.eve.io.creator.impl.sql.ItemsSql;
import net.nikr.eve.io.creator.impl.yaml.ItemsYaml;
import net.nikr.eve.io.creator.impl.sql.JumpsSql;
import net.nikr.eve.io.creator.impl.yaml.JumpsYaml;
import net.nikr.eve.io.creator.impl.sql.LocationsSql;
import net.nikr.eve.io.creator.impl.yaml.LocationsYaml;
import net.nikr.eve.io.creator.impl.Version;

/**
 *
 * @author Andrew Wheat
 */
public enum Creators {
	LOCATIONS_SQL(new LocationsSql())
	, ITEMS_SQL(new ItemsSql())
	, JUMPS_SQL(new JumpsSql())
	, FLAGS_SQL(new FlagsSql())
	, LOCATIONS_YAML(new LocationsYaml())
	, ITEMS_YAML(new ItemsYaml())
	, JUMPS_YAML(new JumpsYaml())
	, FLAGS_YAML(new FlagsYaml())
	, VERSION(new Version())
	;


	Creator creator;

	private Creators(Creator creator) {
		this.creator = creator;
	}

	public Creator getCreator() {
		return creator;
	}
}
