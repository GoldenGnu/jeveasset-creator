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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import net.nikr.eve.ConnectionData;
import net.nikr.eve.Program;
import net.nikr.log.Log;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ConnectionReader extends AbstractXmlReader {

	private ConnectionReader() {}

	public static Connection loadConnection(){
		ConnectionData connectionData = null;
		try {
			String fName = Program.getFilename("connection.xml");
			System.out.println("fName = " + fName);
			Element element = getDocumentElement(fName);
			connectionData = parseConnection(element);
		} catch (IOException ex) {
			Log.error("Connection not loaded: "+ex.getMessage(), ex);
		} catch (XmlException ex) {
			Log.error("Connection not loaded: "+ex.getMessage(), ex);
		}
		Log.info("Connection loaded");
		Connection con = null;
		try {
			Class.forName(connectionData.getDriver());
			String connectionUrl = connectionData.getConnectionUrl();
			con = DriverManager.getConnection(connectionUrl, connectionData.getUsername(), connectionData.getPassword());
		} catch (ClassNotFoundException ex) {
			Log.error("Connecting to SQL server failed (SQL): "+ex.getMessage(), ex);
		} catch (SQLException ex) {
			Log.error("Connecting to SQL server failed (SQL): "+ex.getMessage(), ex);
		}
		Log.info("Connection opened");
		return con;
	}

	private static ConnectionData parseConnection(Element element) throws XmlException {
		ConnectionData connectionData = new ConnectionData();
		if (!element.getNodeName().equals("connection")) {
			throw new XmlException("Wrong root element name.");
		}

		//Host
		NodeList hostNode = element.getElementsByTagName("host");
		if (hostNode.getLength() == 1){
			Element hostElement = (Element) hostNode.item(0);
			connectionData.setHost(hostElement.getTextContent());
		} else {
			throw new XmlException("Wrong host element count.");
		}
		

		//Port
		NodeList portNode = element.getElementsByTagName("port");
		if (portNode.getLength() == 1){
			Element portElement = (Element) portNode.item(0);
			connectionData.setPort(portElement.getTextContent());
		} else {
			throw new XmlException("Wrong port element count.");
		}
		

		//Database
		NodeList databaseNode = element.getElementsByTagName("database");
		if (databaseNode.getLength() == 1){
			Element databaseElement = (Element) databaseNode.item(0);
			connectionData.setDatabase(databaseElement.getTextContent());
		} else {
			throw new XmlException("Wrong database element count.");
		}
		

		//Username
		NodeList usernameNode = element.getElementsByTagName("username");
		if (usernameNode.getLength() == 1){
			Element usernameElement = (Element) usernameNode.item(0);
			connectionData.setUsername(usernameElement.getTextContent());
		} else {
			throw new XmlException("Wrong username element count.");
		}
		

		//Password
		NodeList passwordNode = element.getElementsByTagName("password");
		if (passwordNode.getLength() == 1){
			Element passwordElement = (Element) passwordNode.item(0);
			connectionData.setPassword(passwordElement.getTextContent());
		} else {
			throw new XmlException("Wrong password element count.");
		}

		//Driver
		NodeList driverNode = element.getElementsByTagName("driver");
		if (driverNode.getLength() == 1){
			Element driverElement = (Element) driverNode.item(0);
			connectionData.setDriver(driverElement.getTextContent());
		} else {
			throw new XmlException("Wrong driver element count.");
		}

		//URL Part
		NodeList driverURLPartNode = element.getElementsByTagName("driverURLPart");
		if (driverURLPartNode.getLength() == 1){
			Element driverURLPartElement = (Element) driverURLPartNode.item(0);
			connectionData.setDriverURLPart(driverURLPartElement.getTextContent());
		} else {
			throw new XmlException("Wrong driver URL part element count.");
		}
		
		return connectionData;
	}
}
