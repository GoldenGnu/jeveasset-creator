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
import java.util.ArrayList;
import java.util.List;
import net.nikr.eve.gui.Frame;
import net.nikr.eve.io.creator.Creator;


public class DataWriter extends Thread{

	private Frame frame;
	private List<Creator> creators;
	private Connection con;
	List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>();

	public DataWriter(Frame frame, List<Creator> creators, Connection con) {
		this.frame = frame;
		this.creators = creators;
		this.con = con;
	}

	public boolean addProgressMonitor(ProgressMonitor e) {
		e.setIndeterminate(true);
		return progressMonitors.add(e);
	}

	@Override
	public void run() {
		frame.startRun();

		for (ProgressMonitor pm : progressMonitors) {
			pm.setIndeterminate(false);
			pm.setMaximum(creators.size());
			pm.setMinimum(0);
			pm.setValue(0);
		}

		int count = 0;
		for (Creator creator : creators) {
			for (ProgressMonitor pm : progressMonitors) {
				pm.setValue(count);
			}
			creator.create(null, con);
			++count;
		}
		for (ProgressMonitor pm : progressMonitors) {
			pm.setValue(count);
		}

		frame.endRun();
	}
}
