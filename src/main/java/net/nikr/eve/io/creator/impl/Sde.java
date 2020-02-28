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
package net.nikr.eve.io.creator.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import net.nikr.eve.Main;
import net.nikr.eve.Program;
import net.nikr.eve.Program.Worker;
import net.nikr.eve.Settings;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.util.Duration;
import net.nikr.eve.util.MD5;
import org.slf4j.LoggerFactory;


public class Sde implements Creator {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(Sde.class);

	private final static String URL_MD5 = "https://eve-static-data-export.s3-eu-west-1.amazonaws.com/tranquility/checksum";
	private final static String URL_SDE = "https://eve-static-data-export.s3-eu-west-1.amazonaws.com/tranquility/sde.zip";
	private final static String SDE = "sde.zip";

	@Override
	public boolean create() {
		Duration duration = new Duration();
		duration.start();
		LOG.info("SDE:");
		LOG.info("	Downloading MD5...");
		String downloadMD5 = MD5.download(URL_MD5);
		if (downloadMD5 == null) {
			LOG.error("	-!- Failed to download md5");
			return false;
		}
		File sde = Program.getUserFile(SDE);
		//SDE outdated -> download
		if (!validateSDE(sde, downloadMD5)) {
			LOG.info("	SDE outdated: download");
			if (Settings.isAuto() || Program.run(new ConfirmDialog("Download the latest SDE to cache?", "SDE outdated"))) {
				//Delete unzipped sde folder, as it needs to be updated after download
				LOG.info("		Deleteing unzipped SDE...");
				deleteDirectory(Program.getUserFile("sde"));
				//Download sde
				LOG.info("		Downloading SDE...");
				sde = downloadSde();
				if (!validateSDE(sde, downloadMD5)) {
					LOG.error("		-!- Failed to download SDE");
					return false;
				}
			}
		} else { //SDE current
			if (!Settings.isAuto() && !Program.run(new ConfirmDialog("Current SDE found in cache.\r\nUse it to generate the data files?", "SDE current"))) {
				sde = null;
			} else {
				LOG.info("	SDE is latest");
			}
		}
		//SDE outdated -> browse
		if (!Settings.isAuto() && !validateSDE(sde, downloadMD5)) {
			sde = Program.run(new OpenZip());
			if (sde == null) {
				return false;
			}
			if (!validateSDE(sde, downloadMD5)) {
				if (!Program.run(new ConfirmDialog("The selected SDE is outdated.\r\nUse anyway?", "SDE outdated"))) {
					return false;
				}
			}
		}
		try {
			if (!Program.getUserFile("sde").exists()) {
				LOG.info("	Unzipping SDE...");
				return unzip(sde);
			} else {
				return true;
			}
		} finally {
			duration.end();
			LOG.info("	SDE done in " + duration.getString());
		}
	}

	@Override
	public String getName() {
		return "SDE";
	}

	@Override
	public File getFile() {
		return null;
	}

	private boolean validateSDE(File sde, String matchMD5) {
		LOG.info("	Validating SDE...");
		if (sde == null || !sde.exists()) {
			return false;
		}
		String fileMD5 = MD5.zip(sde);
		if (fileMD5 == null) {
			LOG.error("	-!- Failed to hash file");
			return false;
		}
		return fileMD5.equals(matchMD5);
	}

	private File downloadSde() {
		InputStream in = null;
		try {
			in = new URL(URL_SDE).openStream();
			File sde = Program.getUserFile(SDE);
			if (sde == null) {
				return null;
			}
			Files.copy(in, sde.toPath(), StandardCopyOption.REPLACE_EXISTING);
			return sde;
		} catch (IOException ex) {
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				//No problem
			}
		}
	}

	boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	private boolean unzip(File zipFile) {
		// create output directory if it doesn't exist
		File destDir = Program.getUserFile("");
		if (destDir == null) {
			return false;
		}
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		FileInputStream fis;
		//buffer for read and write data to file
		byte[] buffer = new byte[1024];
		try {
			fis = new FileInputStream(zipFile);
			try (ZipInputStream zis = new ZipInputStream(fis)) {
				ZipEntry ze = zis.getNextEntry();
				while (ze != null) {
					String fileName = ze.getName();
					File newFile = new File(destDir + File.separator + fileName);
					//create directories for sub directories in zip
					new File(newFile.getParent()).mkdirs();
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
					}
					//close this ZipEntry
					zis.closeEntry();
					ze = zis.getNextEntry();
				}
				//close last ZipEntry
				zis.closeEntry();
			}
			fis.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private static class OpenZip extends Worker<File> {

		private File file = null;

		@Override
		public void run() {
			Main.initLookAndFeel();
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jFileChooser.setMultiSelectionEnabled(false);
			jFileChooser.setAcceptAllFileFilterUsed(false);
			jFileChooser.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					}
					return f.getName().endsWith(".zip");
				}

				@Override
				public String getDescription() {
					return "Zip Files";
				}
			});
			int showOpenDialog = jFileChooser.showOpenDialog(window);
			if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
				file = jFileChooser.getSelectedFile();
			} else {
				file = null;
			}

		}


		@Override
		public File get() {
			return file;
		}
	}

	private static class ConfirmDialog extends Worker<Boolean> {

		private final String msg;
		private final String title;

		public ConfirmDialog(String msg, String title) {
			this.msg = msg;
			this.title = title;
		}
		
		private boolean ok = false;

		@Override
		public void run() {
			Main.initLookAndFeel();
			//int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Project folder sde.zip is update to date\r\nContinue?", "SDE current", JOptionPane.OK_CANCEL_OPTION);
			int showConfirmDialog = JOptionPane.showConfirmDialog(window, msg, title, JOptionPane.OK_CANCEL_OPTION);
			ok = (showConfirmDialog == JOptionPane.OK_OPTION);
		}

		@Override
		public Boolean get() {
			return ok;
		}
	}
}
