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
package net.nikr.eve.io.creator.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import net.nikr.eve.Main;
import net.nikr.eve.Program;
import net.nikr.eve.Program.Worker;
import net.nikr.eve.Settings;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Version extends AbstractXmlWriter implements Creator {

	private final static Logger LOG = LoggerFactory.getLogger(Version.class);

	@Override
	public boolean create() {
		LOG.info("Version:");
		String version;
		if (Settings.isAuto()) {
			version = getVersion();
			LOG.info("	Auto: " + version);
		} else {
			LOG.info("	Asking user...");
			version = Program.run(new VersionGetter());
			LOG.info("	Set to: " + version);

		}
		if (version == null) {
			return false;
		}
		return createVersion(version);
	}

	public boolean createVersion(String version) {
		BufferedWriter writer = null;
		InputStream input = null;
		int n;
		try {
			//create a temporary file
			File file = Program.getDataFile("data.dat");
			MessageDigest md = MessageDigest.getInstance("MD5");
			writer = new BufferedWriter(new OutputStreamWriter(new DigestOutputStream(new FileOutputStream(file), md)));
			writer.write(version);
			writer.close();
			//Hash file
			createHashFile(md, file);
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			LOG.error(ex.getMessage(), ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
					LOG.error(ex.getMessage(), ex);
				}
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return "data.dat";
	}

	@Override
	public File getFile() {
		return null;
	}

	public static String getVersion() {
		Date today = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		return getVersion(format.format(today), getOnlineVersion());
	}

	public static String getVersion(String local, String online) {
		if (local == null) {
			return null;
		}
		if (online == null) {
			return local;
		}
		if (local.equals(online.replaceAll("[a-zA-Z]", ""))) { //Same date
			if (online.length() == 10) { //identical
				return local + "a";
			} else { //already have one or more letters
				//Get last letter
				char[] chars = online.toCharArray();
				char c = chars[chars.length-1];
				if (c == 'z') { //Letter sequenze reached the end, adding new letter
					return online + "a";
				} else { //Replace last letter with the letter next in the alphabet
					chars[chars.length-1] = (char) (c + 1);
					return String.valueOf(chars);
				}
			}
		} else { //New date
			return local;
		}
	}

	private static String getOnlineVersion() {
		try {
			StringBuilder builder = new StringBuilder();
			URL url = new URL(OnlineOutdated.REPO + "data.dat");
			try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					builder.append(inputLine);
				}
				return builder.toString();
			}
		} catch (IOException ex) {
			return null;
		}
	}

	private static class VersionGetter extends Worker<String> {

		private String version = null;

		@Override
		public void run() {
			Main.initLookAndFeel();

			version = (String) JOptionPane.showInputDialog(window, "Enter version: YYYY-MM-DD[a-z]", "Version", JOptionPane.QUESTION_MESSAGE, null, null, getVersion());
		}

		@Override
		public String get() {
			return version;
		}
	}
}
