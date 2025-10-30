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
package net.nikr.eve.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author nkr
 */
public class MD5 {
	public static String file(File file) {
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

	public static String download(String md5, String file) {
		try {
			URL url = new URL(md5);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					String line = inputLine.toLowerCase();
					if (line.endsWith(file)) {
						return line.replace(file, "").trim();
					}
				}
			}
		} catch (IOException ex) {
			
		}
		return null;
	}

	public static String download(String md5) {
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
}
