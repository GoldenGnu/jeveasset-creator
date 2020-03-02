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

import net.nikr.eve.io.creator.CreatorType;
import net.nikr.eve.util.Duration;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SafeDeployTest {
	private final static Logger LOG = LoggerFactory.getLogger(SafeDeployTest.class);

	@Test
	public void test() throws Exception {
		LOG.info("--- Safe Deploy ---");
		Settings.setFailOnCurrent(true);
		Settings.setAuto(true);
		Duration duration = new Duration();
		duration.start();
		for (CreatorType creators : CreatorType.values()) {
			final boolean ok = creators.getCreator().create();
			//assertTrue(ok);
		}
		duration.end();
		LOG.info("Everything completed in: " + duration.getString());
	}

	public static void main(String[] args) {
		SafeDeployTest test = new SafeDeployTest();
		try {
			test.test();
		} catch (Exception ex) {
			
		}
	}
}
