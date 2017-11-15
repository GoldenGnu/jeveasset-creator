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

package net.nikr.eve.io.online;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.me.candle.eve.pricing.Pricing;
import uk.me.candle.eve.pricing.PricingFactory;
import uk.me.candle.eve.pricing.PricingListener;
import uk.me.candle.eve.pricing.options.LocationType;
import uk.me.candle.eve.pricing.options.PricingFetch;
import uk.me.candle.eve.pricing.options.PricingNumber;
import uk.me.candle.eve.pricing.options.PricingOptions;
import uk.me.candle.eve.pricing.options.PricingType;


public class EveCentralTest implements PricingListener{

	private final static Logger LOG = LoggerFactory.getLogger(EveCentralTest.class);

	private static boolean skip = false;
	private static PricingFetch PRICING_FETCH = PricingFetch.EVEMARKETER;

	private final Set<Integer> queue = Collections.synchronizedSet(new HashSet<Integer>()) ;
	private final Set<Integer> blacklist = Collections.synchronizedSet(new HashSet<Integer>()) ;

	private EveCentralTest() {}

	public static Set<Integer> testEveCentral(Set<Integer> typeIDs) {
		EveCentralTest eveCentralTest = new EveCentralTest();
		return eveCentralTest.runTest(typeIDs);
	}

	private Set<Integer> runTest(Set<Integer> typeIDs) {
		if (skip) {
			LOG.warn("Skipping EveCentral Test");
			return new HashSet<Integer>();
		}
		Level level = org.apache.log4j.Logger.getRootLogger().getLevel();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
		Pricing pricing = PricingFactory.getPricing(new DefaultPricingOptions());
		queue.clear();
		blacklist.clear();
		queue.addAll(typeIDs);
		pricing.addPricingListener(this);
		pricing.updatePrices(typeIDs);
		int percentLast = 0;
		while (!queue.isEmpty()) {
			synchronized(this) {
				try {
					wait(1000);
				} catch (InterruptedException ex) {
					//No problem
				}
			}
			double done = typeIDs.size() - queue.size();
			int percent = (int)(done * 100.0 /  typeIDs.size());
			if (percent > percentLast) {
				percentLast = percent;
				switch (percent) {
					case 100:
						System.out.print("!");
						break;
					case 75:
						System.out.print("3/4");
						break;
					case 50:
						System.out.print("1/2");
						break;
					case 25:
						System.out.print("1/4");
						break;
					default:
						System.out.print(".");
						break;
				}
			}
		}
		System.out.print("\r\n");
		org.apache.log4j.Logger.getRootLogger().setLevel(level);
		return blacklist;
	}

	@Override
	public void priceUpdated(int typeID, Pricing pricing) {
		queue.remove(typeID);
		synchronized(this) {
			notifyAll();
		}
	}

	@Override
	public void priceUpdateFailed(int typeID, Pricing pricing) {
		blacklist.add(typeID);
		queue.remove(typeID);
		synchronized(this) {
			notifyAll();
		}
	}

	public static boolean isSkip() {
		return skip;
	}

	public static void setSkip(boolean skip) {
		EveCentralTest.skip = skip;
	}

	public static void setPricingFetch(PricingFetch pricingFetch) {
		EveCentralTest.PRICING_FETCH = pricingFetch;
	}

	private static class DefaultPricingOptions implements PricingOptions {

		@Override
		public long getPriceCacheTimer() {
			return 1 * 60 * 60 * 1000L;
		}

		@Override
		public PricingFetch getPricingFetchImplementation() {
			return PRICING_FETCH;
		}

		@Override
		public LocationType getLocationType() {
			return LocationType.REGION;
		}

		@Override
		public List<Long> getLocations() {
			return Collections.singletonList(10000002L); //Caldari: The Forge
		}

		@Override
		public PricingType getPricingType() {
			return PricingType.LOW;
		}

		@Override
		public PricingNumber getPricingNumber() {
			return PricingNumber.SELL;
		}

		@Override
		public InputStream getCacheInputStream() throws IOException {
			return null;
		}

		@Override
		public OutputStream getCacheOutputStream() throws IOException {
			return null;
		}

		@Override
		public boolean getCacheTimersEnabled() {
			return false;
		}

		@Override
		public Proxy getProxy() {
			return Proxy.NO_PROXY;
		}

		@Override
		public int getAttemptCount() {
			return 1;
		}

		@Override
		public boolean getUseBinaryErrorSearch() {
			return true;
		}

		@Override
		public int getTimeout() {
			return 20;
		}
	}
}
