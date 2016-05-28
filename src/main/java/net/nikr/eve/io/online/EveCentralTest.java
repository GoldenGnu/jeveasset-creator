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
import java.util.Collection;
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

	private static boolean skip;

	private Set<Integer> queue = new HashSet<Integer>();
	private Set<Integer> blacklist = new HashSet<Integer>();

	private EveCentralTest() {}

	public static Set<Integer> testEveCentral(Set<Integer> typeIDs) {
		EveCentralTest eveCentralTest = new EveCentralTest();
		return eveCentralTest.runTest(typeIDs);
	}

	private Set<Integer> runTest(Set<Integer> typeIDs) {
		if (skip) {
			LOG.warn("Skipping EveCentral Test");
			return blacklist;
		}
		Level level = org.apache.log4j.Logger.getRootLogger().getLevel();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.ERROR);
		Pricing pricing = PricingFactory.getPricing(new DefaultPricingOptions());
		clear();
		addAll(typeIDs);
		pricing.addPricingListener(this);
		for (int typeID : typeIDs) {
			createPrice(pricing, typeID);
		}
		int percentLast = 0;
		while (!isEmpty()) {
			synchronized(this) {
				try {
					wait();
				} catch (InterruptedException ex) {
					//No problem
				}
			}
			double done = typeIDs.size() - size();
			int percent = (int)(done * 100.0 /  typeIDs.size());
			if (percent > percentLast) {
				percentLast = percent;
				if (percent == 100) {
					System.out.print("!");
				} else if (percent == 75) {
					System.out.print("3/4");
				} else if (percent == 50) {
					System.out.print("1/2");
				} else if (percent == 25) {
					System.out.print("1/4");
				} else {
					System.out.print(".");
				}
			}
		}
		System.out.print("\r\n");
		org.apache.log4j.Logger.getRootLogger().setLevel(level);
		return blacklist;
	}

	private synchronized void clear() {
		queue.clear();
	}

	private synchronized boolean addAll(Collection<? extends Integer> c) {
		return queue.addAll(c);
	}

	private synchronized boolean isEmpty() {
		return queue.isEmpty();
	}

	private synchronized int size() {
		return queue.size();
	}

	private synchronized void remove(int i) {
		queue.remove(i);
	}

	private void createPrice(Pricing pricing, int typeID) {
		Double price = pricing.getPrice(typeID);
		if (price != null) {
			remove(typeID);
		}
	}

	@Override
	public void priceUpdated(int typeID, Pricing pricing) {
		createPrice(pricing, typeID);
		synchronized(this) {
			notifyAll();
		}
	}

	@Override
	public void priceUpdateFailed(int typeID, Pricing pricing) {
		blacklist.add(typeID);
		remove(typeID);
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
	
	private static class DefaultPricingOptions implements PricingOptions {

		@Override
		public long getPriceCacheTimer() {
			return 1 * 60 * 60 * 1000L;
		}

		@Override
		public PricingFetch getPricingFetchImplementation() {
			return PricingFetch.EVE_CENTRAL;
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
	}
}
