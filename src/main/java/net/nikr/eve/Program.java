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

package net.nikr.eve;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import net.nikr.eve.gui.Frame;
import net.nikr.eve.io.ConnectionReader;
import net.nikr.eve.io.XmlException;


public class Program {

	public static final String PROGRAM_VERSION = "1.0.0";
	public static final String PROGRAM_NAME = "XML Creator for jEveAssets";


	public Program() {
		Connection con = ConnectionReader.loadConnection();
		Frame frame = new Frame(con);
		frame.setVisible(true);
	}

	public static String getFilename(String filename) throws XmlException{
		try {
			File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			return file.getAbsolutePath()+File.separator+filename;
		} catch (URISyntaxException ex) {
			throw new XmlException(ex);
		}
	}

}
