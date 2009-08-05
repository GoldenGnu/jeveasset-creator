/*
 * Copyright 2009, Niklas Kyster Rasmussen
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

package net.nikr.eve.io;

import java.sql.Connection;
import net.nikr.eve.gui.Frame;


public class DataWriter extends Thread{

	private Frame frame;
	private boolean runLocation;
	private boolean runItems;
	private Connection con;

	public DataWriter(Frame frame, boolean runLocation, boolean runItems, Connection con) {
		this.frame = frame;
		this.runLocation = runLocation;
		this.runItems = runItems;
		this.con = con;
	}

	@Override
	public void run() {
		frame.startRun();
		String locationsText = "";
		String itemsText = "";
		if (runLocation){
			if (Locations.saveLocations(con)){
				locationsText = "<html><span style=\"color: #006600;\">OK</span>";
			} else {
				locationsText = "<html><span style=\"color: #aa0000;\">FAILED</span>";
			}

		}
		if (runItems) {
			if (Items.saveItems(con)){
				itemsText = "<html><span style=\"color: #006600;\">OK</span>";
			} else {
				itemsText = "<html><span style=\"color: #aa0000;\">FAILED</span>";
			}
		}
		frame.endRun(locationsText, itemsText);
	}
}
