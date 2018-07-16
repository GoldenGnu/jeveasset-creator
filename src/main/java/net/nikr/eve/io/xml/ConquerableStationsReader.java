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

package net.nikr.eve.io.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.nikr.eve.io.data.map.ConquerableStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public final class ConquerableStationsReader extends AbstractXmlReader {

	private static final Logger LOG = LoggerFactory.getLogger(ConquerableStationsReader.class);

	private ConquerableStationsReader() { }

	public static List<ConquerableStation> load() {
		ConquerableStationsReader reader = new ConquerableStationsReader();
		return reader.read();
	}

	private List<ConquerableStation> read() {
		try {
			Element element = getDocumentElement("conquerable_stations.xml");
			List<ConquerableStation> conquerableStations = new ArrayList<>();
			parse(element, conquerableStations);
			return conquerableStations;
		} catch (IOException ex) {
			LOG.info("Conquerable stations not loaded");
		} catch (XmlException ex) {
			LOG.error("Conquerable stations not loaded: " + ex.getMessage(), ex);
		}
		LOG.info("Conquerable stations loaded");
		return null;
	}

	private void parse(final Element element, final List<ConquerableStation> conquerableStations) throws XmlException {
		if (!element.getNodeName().equals("stations")) {
			throw new XmlException("Wrong root element name.");
		}
		parseConquerableStations(element, conquerableStations);
	}

	private void parseConquerableStations(final Element element, final List<ConquerableStation> conquerableStations) throws XmlException {
		NodeList filterNodes = element.getElementsByTagName("station");
		for (int i = 0; i < filterNodes.getLength(); i++) {
			Element currentNode = (Element) filterNodes.item(i);
			ConquerableStation station = parseConquerableStation(currentNode);
			conquerableStations.add(station);
		}
	}

	private ConquerableStation parseConquerableStation(final Element element) throws XmlException {
		int locationID =  getInt(element, "locationid");
		int stationID = getInt(element, "systemid");
		return new ConquerableStation(locationID, stationID);
	}

	public static int getInt(final Node node, final String attributeName) throws XmlException {
		String value = getNodeValue(node, attributeName);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new XmlException("Failed to convert value: " +value+ " to Integer form node: " + node.getNodeName() + " > " + attributeName);
		}
	}

	private static String getNodeValue(final Node node, final String attributeName) throws XmlException {
		Node attributeNode = node.getAttributes().getNamedItem(attributeName);
		if (attributeNode == null) {
			throw new XmlException("Failed to parse attribute from node: " + node.getNodeName() + " > " + attributeName);
		}
		return attributeNode.getNodeValue();
	}
}
