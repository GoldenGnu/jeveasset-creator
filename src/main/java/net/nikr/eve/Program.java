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

package net.nikr.eve;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.SwingUtilities;
import javax.xml.bind.DatatypeConverter;
import net.nikr.eve.gui.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Program {

	private final static Logger LOG = LoggerFactory.getLogger(Program.class);

	public static final String PROGRAM_VERSION = "1.0.0";
	public static final String PROGRAM_NAME = "XML Creator for jEveAssets";

	private static Window window = null;
	
	public Program() {
		Settings.setFailOnOutdated(true);
		//Create GUI
		window = new MainFrame();
		//Show GUI
		window.setVisible(true);
	}

	public static File getDataFile(String filename) {
		try {
			File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			File ret = new File(file.getAbsolutePath()+File.separator+"data"+File.separator+filename);
			if (!ret.getParentFile().exists()) ret.getParentFile().mkdirs();
			return ret;
		} catch (URISyntaxException ex) {
			return null;
		}
	}

	public static File getUserFile(String filename) {
		File file = new File(System.getProperty("user.home"));
		File ret = new File(file.getAbsolutePath()+File.separator+".jeveassets-creator-cache"+File.separator+filename);
		if (!ret.getParentFile().exists()) ret.getParentFile().mkdirs();
		return ret;
	}

	public static String fileMD5(File file) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(Files.readAllBytes(file.toPath()));
			return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
		} catch (IOException ex) {
			return null;
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static String downloadMd5(String md5) {
		try {
			StringBuilder builder = new StringBuilder();
			URL url = new URL(md5);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					builder.append(inputLine);
				}
				return builder.toString().toLowerCase();
			}
		} catch (IOException ex) {
			return null;
		}
	}

	public static <T> T run(Worker<T> worker) {
		worker.setWindow(window);
		try {
			SwingUtilities.invokeAndWait(worker);
		} catch (InterruptedException ex) {
			
		} catch (InvocationTargetException ex) {

		}
		return worker.get();
	}

	public static abstract class Worker<T> implements Runnable {
		protected Window window = null;
		public abstract T get();
		public void setWindow(Window window) {
			this.window = window;
		}
	}
}
