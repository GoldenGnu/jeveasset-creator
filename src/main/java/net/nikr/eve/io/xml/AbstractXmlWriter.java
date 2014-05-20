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

package net.nikr.eve.io.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;


public abstract class AbstractXmlWriter {

	private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractXmlWriter.class);

	protected Document getXmlDocument(String rootname) throws XmlException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			return impl.createDocument(null, rootname, null);
		} catch (ParserConfigurationException ex) {
			throw new XmlException(ex.getMessage(), ex);
		}
	}
	protected void writeXmlFile(Document doc, String filename) throws XmlException {
		writeXmlFile(doc, filename, "UTF-8");
	}

	protected void writeXmlFile(Document doc, String filename, String encoding) throws XmlException {
		DOMSource source = new DOMSource(doc);
		try {
			File outputFile = new File(filename);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestOutputStream digestOutputStream = new DigestOutputStream(outputStream, md);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(digestOutputStream, encoding);
			// result
			Result result = new StreamResult(outputStreamWriter);

			TransformerFactory factory = TransformerFactory.newInstance();
			factory.setAttribute("indent-number", 4);

			Transformer transformer;
			transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
			transformer.transform(source, result);
			createHashFile(md, filename);
		} catch (FileNotFoundException ex) {
			throw new XmlException(ex.getMessage(), ex);
		} catch (TransformerConfigurationException ex) {
			throw new XmlException(ex.getMessage(), ex);
		} catch (TransformerException ex) {
			throw new XmlException(ex.getMessage(), ex);
		} catch (UnsupportedEncodingException ex) {
			throw new XmlException(ex.getMessage(), ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new XmlException(ex.getMessage(), ex);
		}
	}

	protected boolean createHashFile(MessageDigest md, String filename) throws XmlException {
		BufferedWriter writer = null;
		try {
			File file = new File(filename + ".md5");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(getToHex(md.digest()));
		} catch (IOException ex) {
			throw new XmlException(ex.getMessage(), ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					throw new XmlException(ex.getMessage(), ex);
				}
			}
		}
		return false;
	}

	private String getToHex(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
}
