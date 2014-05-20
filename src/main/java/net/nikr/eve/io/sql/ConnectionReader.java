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

package net.nikr.eve.io.sql;

import java.io.IOException;
import javax.swing.JOptionPane;
import net.nikr.eve.Program;
import net.nikr.eve.io.xml.AbstractXmlReader;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ConnectionReader extends AbstractXmlReader {

	private final static Logger LOG = LoggerFactory.getLogger(ConnectionReader.class);

	private static ConnectionData data = null;
	
	public static ConnectionData getConnectionData(){
		if (data == null) {
			ConnectionReader reader = new ConnectionReader();
			data = reader.parseConnection();
		}
		return data;
	}
	
	private ConnectionReader() {}

	private ConnectionData parseConnection(){
		ConnectionData connectionData = null;
		try {
			String fName = Program.getFilename("connection.xml");
			System.out.println("fName = " + fName);
			Element element = getDocumentElement(fName);
			connectionData = parseConnection(element);
			if (connectionData.getPassword().isEmpty()){
				String password = JOptionPane.showInputDialog(null, "Enter MySQL password:", "Password", JOptionPane.PLAIN_MESSAGE);
				if (password != null){
					connectionData.setPassword(password);
				}
			}
			if (connectionData.getDatabase().isEmpty()){
				String database = JOptionPane.showInputDialog(null, "Enter MySQL database name:", "Database", JOptionPane.PLAIN_MESSAGE);
				if (database != null){
					connectionData.setDatabase(database);
				}
			}
			LOG.info("Connection loaded");
		} catch (IOException ex) {
			LOG.error("Connection not loaded: "+ex.getMessage(), ex);
		} catch (XmlException ex) {
			LOG.error("Connection not loaded: "+ex.getMessage(), ex);
		}
		return connectionData;
	}

	private ConnectionData parseConnection(Element element) throws XmlException {
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
