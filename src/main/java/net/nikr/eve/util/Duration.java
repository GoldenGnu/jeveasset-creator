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

package net.nikr.eve.util;


public class Duration {

	private long start;
	private long end;

	public void start() {
		start = System.nanoTime();
	}

	public void end() {
		end = System.nanoTime();
	}

	public String getString() {
		long duration = (end - start) / 1000000;
		StringBuilder build = new StringBuilder();
		int hours   = (int) ((duration / (1000*60*60)) % 24);
		if (hours > 0) {
			build.append(hours);
			build.append("h");
		}
		int minutes = (int) ((duration / (1000*60)) % 60);
		if (minutes > 0) {
			if (hours > 0) {
				build.append(" ");
			}
			build.append(minutes);
			build.append("m");
		}
		int seconds = (int) (duration / 1000) % 60 ;
		if (seconds > 0) {
			if (hours > 0 || minutes > 0) {
				build.append(" ");
			}
			build.append(seconds);
			build.append("s");
		}
		int ms = (int) duration % 1000 ;
		if (ms > 0) {
			if (hours > 0 || minutes > 0 || seconds > 0) {
				build.append(" ");
			}
			build.append(ms);
			build.append("ms");
		}
		return build.toString();
	}
}
