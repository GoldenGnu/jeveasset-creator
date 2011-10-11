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

import java.awt.Color;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import net.nikr.eve.gui.Frame;
import net.nikr.eve.gui.Frame.CreatorSection;
import net.nikr.eve.io.creator.Creator;


public class DataWriter extends Thread{

	private Frame frame;
	private List<Creator> creators;
	private Connection con;
	List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>();
	private List<CreatorSection> creatorSections;

	public DataWriter(Frame frame, List<Creator> creators, List<CreatorSection> creatorSections, Connection con) {
		this.frame = frame;
		this.creators = creators;
		this.creatorSections = creatorSections;
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
		for (int i = 0; i < creators.size(); i++){
			Creator creator = creators.get(i);
			final CreatorSection section = creatorSections.get(i);
			for (ProgressMonitor pm : progressMonitors) {
				pm.setValue(count);
			}
			final boolean ok = creator.create(null, con);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (ok){
						section.getLeft().setForeground(Color.GREEN.darker().darker());
					} else {
						section.getLeft().setForeground(Color.RED.darker().darker());
					}
				}
			});
			
			
			++count;
		}
		for (ProgressMonitor pm : progressMonitors) {
			pm.setValue(count);
		}

		frame.endRun();
	}
}
