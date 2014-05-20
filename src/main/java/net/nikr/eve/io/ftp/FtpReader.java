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

package net.nikr.eve.io.ftp;

import java.io.IOException;
import javax.swing.JOptionPane;
import net.nikr.eve.Program;
import net.nikr.eve.io.xml.AbstractXmlReader;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class FtpReader extends AbstractXmlReader {

	private final static Logger LOG = LoggerFactory.getLogger(FtpReader.class);

	private static FtpData data = null;
	
	public static FtpData getFtpData(){
		if (data == null) {
			FtpReader reader = new FtpReader();
			data = reader.parseFTP();
		}
		return data;
	}
	
	private FtpReader() {}

	private FtpData parseFTP(){
		FtpData ftpData = null;
		try {
			String fName = Program.getFilename("ftp.xml");
			System.out.println("fName = " + fName);
			Element element = getDocumentElement(fName);
			ftpData = parseFTP(element);
			if (ftpData.getPassword().isEmpty()){
				String password = JOptionPane.showInputDialog(null, "Enter FTP password:", "Password", JOptionPane.PLAIN_MESSAGE);
				if (password != null){
					ftpData.setPassword(password);
				}
			}
			LOG.info("FTP loaded");
		} catch (IOException ex) {
			LOG.error("FTP not loaded: "+ex.getMessage(), ex);
		} catch (XmlException ex) {
			LOG.error("FTP not loaded: "+ex.getMessage(), ex);
		}
		return ftpData;
	}

	private FtpData parseFTP(Element element) throws XmlException {
		FtpData ftpData = new FtpData();
		if (!element.getNodeName().equals("ftp")) {
			throw new XmlException("Wrong root element name.");
		}

		//Host
		NodeList hostNode = element.getElementsByTagName("host");
		if (hostNode.getLength() == 1){
			Element hostElement = (Element) hostNode.item(0);
			ftpData.setHost(hostElement.getTextContent());
		} else {
			throw new XmlException("Wrong host element count.");
		}

		//URL
		NodeList portNode = element.getElementsByTagName("url");
		if (portNode.getLength() == 1){
			Element portElement = (Element) portNode.item(0);
			ftpData.setUrl(portElement.getTextContent());
		} else {
			throw new XmlException("Wrong port element count.");
		}
		

		//Database
		NodeList databaseNode = element.getElementsByTagName("username");
		if (databaseNode.getLength() == 1){
			Element databaseElement = (Element) databaseNode.item(0);
			ftpData.setUsername(databaseElement.getTextContent());
		} else {
			throw new XmlException("Wrong database element count.");
		}
		

		//Username
		NodeList usernameNode = element.getElementsByTagName("password");
		if (usernameNode.getLength() == 1){
			Element usernameElement = (Element) usernameNode.item(0);
			ftpData.setPassword(usernameElement.getTextContent());
		} else {
			throw new XmlException("Wrong username element count.");
		}
		
		return ftpData;
	}
}
