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

package net.nikr.eve.io.creator;

import net.nikr.eve.io.creator.impl.Agents;
import net.nikr.eve.io.creator.impl.Flags;
import net.nikr.eve.io.creator.impl.Items;
import net.nikr.eve.io.creator.impl.Jumps;
import net.nikr.eve.io.creator.impl.Locations;
import net.nikr.eve.io.creator.impl.NpcCorporations;
import net.nikr.eve.io.creator.impl.OnlineOutdated;
import net.nikr.eve.io.creator.impl.Sde;
import net.nikr.eve.io.creator.impl.Version;

/**
 *
 * @author Andrew Wheat
 */
public enum CreatorType {
	SDE(new Sde())
	, VERSION(new Version())
	, LOCATIONS_YAML(new Locations())
	, ITEMS_YAML(new Items())
	, JUMPS_YAML(new Jumps())
	, FLAGS_YAML(new Flags())
	, AGENTS_YAML(new Agents())
	, NPC_CORPORATIONS_YAML(new NpcCorporations())
	//, DOGMA_HOBOLEAKS(new Dogma())
	, ONLINE_OUTDATED(new OnlineOutdated())
	;


	Creator creator;

	private CreatorType(Creator creator) {
		this.creator = creator;
	}

	public Creator getCreator() {
		return creator;
	}
}
