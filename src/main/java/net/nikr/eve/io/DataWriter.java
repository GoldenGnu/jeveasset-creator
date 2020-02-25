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
package net.nikr.eve.io;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import net.nikr.eve.gui.MainFrame;
import net.nikr.eve.gui.MainFrame.CreatorSection;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.creator.impl.Sde;
import org.slf4j.LoggerFactory;

public class DataWriter extends Thread {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DataWriter.class);

	private final MainFrame frame;
	private final List<Creator> creators;
	private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>();
	private final List<CreatorSection> creatorSections;

	public DataWriter(MainFrame frame, List<Creator> creators, List<CreatorSection> creatorSections) {
		this.frame = frame;
		this.creators = creators;
		this.creatorSections = creatorSections;
	}

	public boolean addProgressMonitor(ProgressMonitor e) {
		e.setIndeterminate(true);
		return progressMonitors.add(e);
	}

	@Override
	public void run() {
		updateStart();
		int count = 0;
		for (int i = 0; i < creators.size(); i++) {
			Creator creator = creators.get(i);
			final CreatorSection section = creatorSections.get(i);
			updateBefore(section);
			final boolean ok = creator.create();
			++count;
			updateAfter(section, count, ok);
			if (creator instanceof Sde && !ok) {
				break;
			}
		}
		updateEnd();
	}

	public void updateStart() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.startRun();
				for (ProgressMonitor pm : progressMonitors) {
					pm.setIndeterminate(false);
					pm.setMaximum(creators.size());
					pm.setMinimum(0);
					pm.setValue(0);
				}

			}
		});
	}

	public void updateBefore(final CreatorSection section) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				section.getCheckBox().setForeground(Color.BLUE);
			}
		});
	}
	public void updateAfter(final CreatorSection section, final int count, final boolean ok) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (ok) {
						section.getCheckBox().setForeground(Color.GREEN.darker().darker());
					} else {
						section.getCheckBox().setForeground(Color.RED.darker().darker());
					}
					for (ProgressMonitor pm : progressMonitors) {
						pm.setValue(count);
					}
				}
			});
	}

	public void updateEnd() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.endRun();
			}
		});
	}
}
