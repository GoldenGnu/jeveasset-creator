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

package net.nikr.eve.io.creator.impl.yaml;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.map.Location;
import net.nikr.eve.io.data.map.LocationID;
import net.nikr.eve.io.data.Name;
import net.nikr.eve.util.Duration;
import net.nikr.eve.io.yaml.LocationsReader;
import net.nikr.eve.io.yaml.NameReader;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class LocationsYaml extends AbstractXmlWriter implements Creator {
	
	private final static Logger LOG = LoggerFactory.getLogger(LocationsYaml.class);

	private final DecimalFormat securityformater = new DecimalFormat("0.0", new DecimalFormatSymbols(new Locale("en")));
	
	@Override
	public boolean create() {
		Duration duration = new Duration();
		duration.start();
		LOG.info("Locations:");
		Document xmldoc;
		boolean success = false;
		try {
			xmldoc = getXmlDocument("rows");
			LOG.info("	XML: init...");
			Comment comment = xmldoc.createComment("Generated from Eve Online Toolkit. ©CCP hf. All rights reserved. Used with permission.");
			xmldoc.getDocumentElement().appendChild(comment);
			success = createLocations(xmldoc);
			LOG.info("	XML: Saving...");
			writeXmlFile(xmldoc, Program.getFilename(getFilename()));
		} catch (XmlException ex) {
			LOG.error("Locations not saved (XML): " + ex.getMessage(), ex);
		}
		duration.end();
		LOG.info("	Locations done in " + duration.getString());
		return success;
	}

	@Override
	public String getName() {
		return "Locations (YAML)";
	}

	@Override
	public String getFilename() {
		return "yaml"+File.separator+"locations.xml";
	}

	private boolean createLocations(Document xmldoc) throws XmlException {
		Element parentNode = xmldoc.getDocumentElement();
		try {
			LOG.info("	YAML: Loading...");
			LOG.info("		Map...");
			LocationsReader locationsLoader = new LocationsReader();
			List<LocationID> locationsIDs = locationsLoader.loadLocations();
			LOG.info("		Names...");
			NameReader nameReader = new NameReader();
			Map<Integer, Name> names = nameReader.loadNames();
			LOG.info("	YAML: Prcessing...");
			Set<Location> locations = new TreeSet<Location>();
			for (LocationID locationID : locationsIDs) {
				int stationID = locationID.getStationID();
				int systemID = locationID.getSystemID();
				int regionID = locationID.getRegionID();
				String stationName;
				String systemName;
				String regionName;
				if (locationID.getRegionID() >= 12000000 && locationID.getRegionID() < 13000000) {
					stationName = ""; //Abyssal Stations doesn't exist
					systemName = "Abyssal System #" + systemID;
					regionName = "Abyssal Region #" + regionID;
				} else {
					stationName = names.get(stationID).getItemName();
					systemName = names.get(systemID).getItemName();
					regionName = names.get(regionID).getItemName();
				}
				float security = locationID.getSecurity();
				if (stationID != 0) { //Station
					Location stationLocation = new Location(stationID, stationName, systemID, systemName, regionID, regionName, security);
					locations.add(stationLocation);
				} else if (systemID != 0) { //System
					Location systemLocation = new Location(0, "", systemID, systemName, regionID, regionName, security);
					locations.add(systemLocation);
				} else if (regionID != 0) { //Region
					Location regionLocation = new Location(0, "", 0, "", regionID, regionName, 0);
					locations.add(regionLocation);
				}
			}
			names = null;
			locationsIDs = null;
			LOG.info("	XML: Creating...");
			for (Location location : locations) {
				Element node = xmldoc.createElementNS(null, "row");
				node.setAttributeNS(null, "si", String.valueOf(location.getStationID()));
				node.setAttributeNS(null, "s", location.getStationName());
				node.setAttributeNS(null, "syi", String.valueOf(location.getSystemID()));
				node.setAttributeNS(null, "sy", location.getSystemName());
				node.setAttributeNS(null, "ri", String.valueOf(location.getRegionID()));
				node.setAttributeNS(null, "r", location.getRegionName());
				node.setAttributeNS(null, "se", roundSecurity(location.getSecurity()));
				parentNode.appendChild(node);
			}
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return false;
	}

	private String roundSecurity(double number) {
		if (number < 0) number = 0;
		number = number * 10;
		number = Math.round(number);
		number = number / 10;
		return securityformater.format(number);
	}
}
