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

package net.nikr.eve.io.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.nikr.eve.Main;
import net.nikr.eve.Program;
import net.nikr.eve.Settings;


public class YamlHelper {

	public static enum SdeFile {
		TYPEIDS("fsd", "typeIDs.yaml"),
		GROUPIDS("fsd", "groupIDs.yaml"),
		CATEGORYIDS("fsd", "categoryIDs.yaml"),
		TYPEDOGMA("fsd", "typeDogma.yaml"),
		METAGROUPS("fsd", "metaGroups.yaml"),
		INVTYPEMATERIALS("fsd", "typeMaterials.yaml"),
		BLUEPRINTS("fsd", "blueprints.yaml"),
		INVNAMES("bsd", "invNames.yaml"),
		INVFLAGS("bsd", "invFlags.yaml"),
		UNIVERSE("fsd" + File.separator + "universe", "");

		private final String dir;
		private final String filename;

		private SdeFile(String dir, String filename) {
			this.dir = dir;
			this.filename = filename;
		}

		public String getString() {
			return dir + File.separator + filename;
		}
	}

	public static String sde = null;

	public static String getFile(SdeFile sdeFile) throws IOException {
		return getSde(sdeFile.getString());
	}

	public static <T, C extends Class<T>> T read(SdeFile sdeFile, C c) throws IOException {
		return read(getSde(sdeFile.getString()), c);
	}

	public static <T, C extends Class<T>> T read(String filename, C c) throws IOException {
		ObjectMapper om = new ObjectMapper(new YAMLFactory());
		return om.readValue(new File(filename), c);
	}

	public static <T> T read(SdeFile sdeFile, TypeReference<T> t) throws IOException {
		return read(getSde(sdeFile.getString()), t);
	}

	public static <T> T read(String filename, TypeReference<T> t) throws IOException {
		ObjectMapper om = new ObjectMapper(new YAMLFactory());
		return om.readValue(new File(filename), t);
	}

	private static String getSde(String sdeFile) {
		if (sde == null) {
			File user = getUserDirectory();
			File project = getProjectDirectory();
			File target = getTargetDirectory();
			if (validSde(user)) {
				sde = user.getAbsolutePath();
			} else if (validSde(project)){
				sde = project.getAbsolutePath();
			} else if (validSde(target)){
				sde = target.getAbsolutePath();
			} else if (!Settings.isAuto()) {
				File file = chooseFile();
				if (file != null) {
					sde = file.getAbsolutePath();
				} else {
					throw new RuntimeException("No SDE found");
				}
			} else {
				throw new RuntimeException("No SDE found");
			}
		}
		return sde + File.separator + sdeFile;
	}

	private static File getUserDirectory() {
		return Program.getUserFile("sde");
	}

	private static File getTargetDirectory() {
		File file;
		URL location = YamlHelper.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			file = new File(location.toURI());
		} catch (URISyntaxException ex) {
			file = new File(location.getPath());
		}
		return new File(file.getParentFile().getAbsolutePath() + File.separator + "sde");
	}

	private static File getProjectDirectory() {
		File file;
		URL location = YamlHelper.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			file = new File(location.toURI());
		} catch (URISyntaxException ex) {
			file = new File(location.getPath());
		}
		return new File(file.getParentFile().getParentFile().getAbsolutePath() + File.separator + "sde");
	}

	private static File chooseFile() {
		FileGetter fileGetter = new FileGetter();
		try {
			SwingUtilities.invokeAndWait(fileGetter);
		} catch (InterruptedException | InvocationTargetException ex) {

		}
		return fileGetter.getFile();
	}

	private static class FileGetter implements Runnable {

		private File file = null;

		@Override
		public void run() {
			Main.initLookAndFeel();
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int rc = jFileChooser.showOpenDialog(null);
			while (rc == JFileChooser.APPROVE_OPTION && !validSde(jFileChooser.getSelectedFile())) {
				JOptionPane.showMessageDialog(null, "The directory is not a valid SDE.", "Open Error", JOptionPane.ERROR_MESSAGE);
				rc = jFileChooser.showOpenDialog(null);
			}
			if (rc == JFileChooser.APPROVE_OPTION) {
				file = jFileChooser.getSelectedFile();
			}
		}

		private File getFile() {
			return file;
		}
	}

	private static boolean validSde(File file) {
		if (!file.exists()) {
			return false;
		}
		String fullFilename = file.getAbsolutePath();
		for (SdeFile sdeFile : SdeFile.values()) {
			File test = new File(fullFilename + File.separator + sdeFile.getString());
			if (!test.exists()) {
				System.out.println("Not found: " + test.getAbsolutePath());
				return false;
			}
		}
		return true;
	}
}
