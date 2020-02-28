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
package net.nikr.eve.io.creator.impl;

import java.io.File;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.creator.CreatorType;
import net.nikr.eve.Settings;
import net.nikr.eve.util.MD5;
import org.slf4j.LoggerFactory;


public class OnlineOutdated implements Creator {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(OnlineOutdated.class);

	public final static String REPO = "https://eve.nikr.net/jeveassets/update/data/data/";

	@Override
	public boolean create() {
		LOG.info("Check Online:");
		boolean outdated = false;
		for (CreatorType creators : CreatorType.values()) {
			File file = creators.getCreator().getFile();
			if (file == null) {
				continue;
			}
			String datafile = file.getName() + ".md5";
			String downloadMd5 = MD5.download(REPO + datafile);
			String fileMD5 = MD5.file(file);
			if (!fileMD5.equals(downloadMd5)) {
				LOG.info("	" + file.getName() + " is outdated online");
				outdated = true;
			}
		}
		if (Settings.isFailOnOutdated() && outdated) {
			LOG.info("	Failed due to outdated");
			return false;
		}
		if (Settings.isFailOnCurrent() && !outdated) {
			LOG.info("	Failed due to current");
			return false;
		}
		if (!outdated) {
			LOG.info("	Everything current");
		}
		return true;
	}

	@Override
	public String getName() {
		return "Online == Local";
	}

	@Override
	public File getFile() {
		return null;
	}
	
}
