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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.nikr.eve.gui.MainFrame;
import net.nikr.eve.io.ftp.FtpReader;
import net.nikr.eve.io.sql.ConnectionData;
import net.nikr.eve.io.sql.ConnectionReader;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Program {

	private final static Logger LOG = LoggerFactory.getLogger(Program.class);

	public static final String PROGRAM_VERSION = "1.0.0";
	public static final String PROGRAM_NAME = "XML Creator for jEveAssets";

	

	public Program() {
		//Test connection
		Connection connection = openConnection();
		close(connection);
		//Create GUI
		MainFrame frame = new MainFrame(ConnectionReader.getConnectionData(), FtpReader.getFtpData());
		//Show GUI
		frame.setVisible(true);
	}

	public static String getFilename(String filename) throws XmlException{
		try {
			File file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
			File ret = new File(file.getAbsolutePath()+File.separator+filename);
			if (!ret.getParentFile().exists()) ret.getParentFile().mkdirs();
			return ret.getAbsolutePath();
		} catch (URISyntaxException ex) {
			throw new XmlException(ex);
		}
	}

	public static Connection openConnection() {
		try {
			ConnectionData connectionData = ConnectionReader.getConnectionData();
			Class.forName(connectionData.getDriver());
			String connectionUrl = connectionData.getConnectionUrl();
			Connection connection = DriverManager.getConnection(connectionUrl, connectionData.getUsername(), connectionData.getPassword());
			return connection;
		} catch (Exception ex) {
			LOG.error("Connecting to SQL server failed (SQL): "+ex.getMessage(), ex);
			throw new RuntimeException("Connecting to SQL server failed (SQL)", ex);
		}
	}

	public static void close(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException ex) {
			LOG.error("Closing SQL result failed (SQL): "+ex.getMessage(), ex);
			throw new RuntimeException("Closing SQL result failed (SQL)", ex);
		}
	}
	public static void close(Statement statement) {
		try {
			if (statement != null) {
				statement.close();
			}
		} catch (SQLException ex) {
			LOG.error("Closing SQL statement failed (SQL): "+ex.getMessage(), ex);
			throw new RuntimeException("Closing SQL statement failed (SQL)", ex);
		}
	}
	public static void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException ex) {
			LOG.error("Closing SQL connection failed (SQL): "+ex.getMessage(), ex);
			throw new RuntimeException("Closing SQL connection failed (SQL)", ex);
		}
	}
}
