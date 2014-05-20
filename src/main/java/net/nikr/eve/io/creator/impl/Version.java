/*
 * Copyright 2009, Niklas Kyster Rasmussen, Flaming Candle
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Version extends AbstractXmlWriter implements Creator {

	private final static Logger LOG = LoggerFactory.getLogger(Version.class);

	@Override
	public boolean create() {
		LOG.info("Version:");
		BufferedWriter writer = null;
		InputStream input = null;
		int n;
		try {
			//create a temporary file
			String filename = Program.getFilename(getFilename());
			File file = new File(filename);
			//MessageDigest md = MessageDigest.getInstance("MD5");
			writer = new BufferedWriter(new FileWriter(file));
			String createVersion = createVersion();
			if (createVersion != null) {
				writer.write(createVersion);
				writer.close();

				//Hash file
				MessageDigest md = MessageDigest.getInstance("MD5");

				byte[] buffer = new byte[4096];
				input = new DigestInputStream(new FileInputStream(file), md);
				while ((n = input.read(buffer)) != -1) {
					//Digest
				}
				createHashFile(md, filename);
				return true;
			}
			
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		} catch (XmlException ex) {
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
					ex.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public String getFilename() {
		return "data" + File.separator + "data.dat";
	}

	private String createVersion() {
		return (String) JOptionPane.showInputDialog(null, "Enter version: [NAME] X.X.X", "Version", JOptionPane.QUESTION_MESSAGE, null, null, getDatabase());
	}

	@Override
	public String getName() {
		return "Version";
	}

	private String getDatabase() {
		Connection connection = Program.openConnection();
		try {
			String value = connection.getMetaData().getURL();
			int start = value.lastIndexOf("/") + 1;
			if (start >= 0 && start <= value.length()) {
				value = value.substring(start);
			}
			value = value.replaceFirst("_", " ").replace("_", ".");
			int end = value.lastIndexOf(".");
			if (end >= 0 && end <= value.length()) {
				value = value.substring(0, end);
			}
			return Character.toUpperCase(value.charAt(0)) + value.substring(1);
		} catch (SQLException ex) {
			LOG.warn("Failed to get database name");
			return "";
		} finally {
			Program.close(connection);
		}
	}

}
