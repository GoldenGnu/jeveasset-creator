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

package net.nikr.eve;

import net.nikr.eve.io.creator.YamlCreators;
import net.nikr.eve.io.online.EveCentralTest;
import net.nikr.eve.io.yaml.YamlHelper;
import net.nikr.eve.util.Duration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;


public class YamlDataBuilderTest {

	private final static Logger LOG = LoggerFactory.getLogger(YamlDataBuilderTest.class);

	@Test
	public void yamlTest() throws Exception {
		EveCentralTest.setSkip(true);
		//Finds SDE Location
		YamlHelper.getFile(YamlHelper.SdeFile.TYPEIDS);
		Duration duration = new Duration();
		duration.start();
		for (YamlCreators creators : YamlCreators.values()) {
			final boolean ok = creators.getCreator().create();
			assertTrue(ok);
		}
		duration.end();
		LOG.info("YAML completed in: " + duration.getString());
	}
}
