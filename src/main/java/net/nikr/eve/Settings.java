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
package net.nikr.eve;


public class Settings {
	private static boolean failOnOutdated = false;
	private static boolean failOnCurrent = false;
	private static boolean auto = false;

	public static boolean isFailOnOutdated() {
		return failOnOutdated;
	}

	public static void setFailOnOutdated(boolean failOnOutdated) {
		Settings.failOnOutdated = failOnOutdated;
	}

	public static boolean isFailOnCurrent() {
		return failOnCurrent;
	}

	public static void setFailOnCurrent(boolean failOnCurrent) {
		Settings.failOnCurrent = failOnCurrent;
	}

	public static boolean isAuto() {
		return auto;
	}

	public static void setAuto(boolean auto) {
		Settings.auto = auto;
	}

}
