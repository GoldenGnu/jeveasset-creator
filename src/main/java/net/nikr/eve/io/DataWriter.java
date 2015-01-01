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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import net.nikr.eve.Program;
import net.nikr.eve.gui.MainFrame;
import net.nikr.eve.gui.MainFrame.CreatorSection;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.sql.ConnectionData;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.LoggerFactory;

public class DataWriter extends Thread {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(DataWriter.class);

	private final MainFrame frame;
	private final List<Creator> creators;
	private final ConnectionData connectionData;
	private final List<ProgressMonitor> progressMonitors = new ArrayList<ProgressMonitor>();
	private final List<CreatorSection> creatorSections;

	public DataWriter(MainFrame frame, List<Creator> creators, List<CreatorSection> creatorSections, ConnectionData connectionData) {
		this.frame = frame;
		this.creators = creators;
		this.creatorSections = creatorSections;
		this.connectionData = connectionData;
	}

	public boolean addProgressMonitor(ProgressMonitor e) {
		e.setIndeterminate(true);
		return progressMonitors.add(e);
	}

	@Override
	public void run() {
		frame.startRun();
		final List<String> filenames = new ArrayList<String>();

		for (ProgressMonitor pm : progressMonitors) {
			pm.setIndeterminate(false);
			pm.setMaximum(creators.size());
			pm.setMinimum(0);
			pm.setValue(0);
		}

		int count = 0;
		for (int i = 0; i < creators.size(); i++) {
			Creator creator = creators.get(i);
			final CreatorSection section = creatorSections.get(i);
			for (ProgressMonitor pm : progressMonitors) {
				pm.setValue(count);
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					section.getCheckBox().setForeground(Color.BLUE);
				}
			});
			final boolean ok = creator.create();
			if (ok) {
				filenames.add(creator.getFilename());
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (ok) {
						section.getCheckBox().setForeground(Color.GREEN.darker().darker());
					} else {
						section.getCheckBox().setForeground(Color.RED.darker().darker());
					}
				}
			});

			++count;
		}
		for (ProgressMonitor pm : progressMonitors) {
			pm.setValue(count);
		}

		zipIt(filenames);
		frame.endRun();
	}

	public void zipIt(List<String> filenames) {

		byte[] buffer = new byte[1024];

		try {
			
			String zipFile = Program.getFilename("data.zip");
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			LOG.info("Zip");
			LOG.info("	Output: " + zipFile);

			for (String filename : filenames) {
				LOG.info("	Adding: " + filename);
				ZipEntry ze = new ZipEntry(filename);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(Program.getFilename(filename));

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
				zos.closeEntry();
			}
			//remember close it
			zos.close();

			System.out.println("Done");
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (XmlException ex) {
			ex.printStackTrace();
		}
	}
}
