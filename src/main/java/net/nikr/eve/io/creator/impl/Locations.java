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

package net.nikr.eve.io.creator.impl;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.nikr.eve.Program;
import net.nikr.eve.io.creator.Creator;
import net.nikr.eve.io.data.Name;
import net.nikr.eve.io.data.map.ConquerableStation;
import net.nikr.eve.io.data.map.Location;
import net.nikr.eve.io.data.map.LocationID;
import net.nikr.eve.io.esi.EsiUpdater;
import static net.nikr.eve.io.esi.EsiUpdater.DATASOURCE;
import static net.nikr.eve.io.esi.EsiUpdater.UNIVERSE_API;
import net.nikr.eve.io.xml.AbstractXmlWriter;
import net.nikr.eve.io.xml.ConquerableStationsReader;
import net.nikr.eve.io.xml.XmlException;
import net.nikr.eve.io.yaml.LocationsReader;
import net.nikr.eve.io.yaml.NameReader;
import net.nikr.eve.util.Duration;
import net.troja.eve.esi.ApiException;
import net.troja.eve.esi.ApiResponse;
import net.troja.eve.esi.model.StationResponse;
import net.troja.eve.esi.model.SystemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



public class Locations extends AbstractXmlWriter implements Creator {

	private final static Logger LOG = LoggerFactory.getLogger(Locations.class);

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
			writeXmlFile(xmldoc, getFile());
		} catch (XmlException ex) {
			LOG.error("Locations not saved (XML): " + ex.getMessage(), ex);
		}
		duration.end();
		LOG.info("	Locations done in " + duration.getString());
		return success;
	}

	@Override
	public String getName() {
		return "locations.xml";
	}

	@Override
	public File getFile() {
		return Program.getDataFile("locations.xml");
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
			LOG.info("	YAML: Processing...");
			Set<Location> locations = new TreeSet<>();
			Map<Integer, Location> systemToLocation = new HashMap<>();
			for (LocationID locationID : locationsIDs) {
				int stationID = locationID.getStationID();
				int systemID = locationID.getSystemID();
				int constellationID = locationID.getConstellationID();
				int regionID = locationID.getRegionID();
				String regionName;
				String stationName = names.get(stationID).getItemName();
				String systemName = names.get(systemID).getItemName();
				String constellationName = names.get(constellationID).getItemName();
				Name regions = names.get(regionID);
				if (regions != null) {
					regionName = names.get(regionID).getItemName();
				} else if (regionID == 10000070){
					regionName = "Pochven";
				} else {
					regionName = "Unknown Region #" + regionID;
				}
				float security = locationID.getSecurity();
				if (stationID != 0) { //Station
					Location stationLocation = new Location(stationID, stationName, systemID, systemName, constellationID, constellationName, regionID, regionName, security);
					locations.add(stationLocation);
				} else if (systemID != 0) { //System
					Location systemLocation = new Location(0, "", systemID, systemName, constellationID, constellationName, regionID, regionName, security);
					systemToLocation.put(systemID, systemLocation);
					locations.add(systemLocation);
				} else if (constellationID != 0) { //Constellations
					Location constellationLocation = new Location(0, "", 0, "", constellationID, constellationName, regionID, regionName, 0);
					locations.add(constellationLocation);
				} else if (regionID != 0) { //Region
					Location regionLocation = new Location(0, "", 0, "", 0, "", regionID, regionName, 0);
					locations.add(regionLocation);
				}
			}
			LOG.info("	ESI: Loading...");
			LOG.info("		Locations...");
			Map<Integer, Location> specialSystemUpdates = new HashMap<>();
			specialSystemUpdates.put(30100000, systemToLocation.get(30100000));
			locations.addAll(updateSpecialSystems(specialSystemUpdates));
			LOG.info("	XML: Loading...");
			List<ConquerableStation> conquerableStations = ConquerableStationsReader.load();
			LOG.info("	XML: Prcessing...");
			for (ConquerableStation conqurableStation : conquerableStations) {
				int fixedLocationID = conqurableStation.getLocationID();
				if (fixedLocationID >= 66000000) {
					if (fixedLocationID < 66014933) {
						fixedLocationID = fixedLocationID - 6000001;
					} else {
						fixedLocationID = fixedLocationID - 6000000;
					}
				}
				int stationID = fixedLocationID;
				int systemID = conqurableStation.getSystemID();
				int regionID = systemToLocation.get(conqurableStation.getSystemID()).getRegionID();
				int constellationID = systemToLocation.get(conqurableStation.getSystemID()).getConstellationID();
				String constellationName = systemToLocation.get(conqurableStation.getSystemID()).getConstellationName();
				float security = systemToLocation.get(conqurableStation.getSystemID()).getSecurity();
				String stationName = "Conquerable Station #" + fixedLocationID;
				String systemName = names.get(systemID).getItemName();
				String regionName = names.get(regionID).getItemName();
				Location stationLocation = new Location(stationID, stationName, systemID, systemName, constellationID, constellationName, regionID, regionName, security);
				locations.add(stationLocation);
			}
			systemToLocation.clear();
			conquerableStations.clear();
			names.clear();
			locationsIDs.clear();
			LOG.info("	XML: Creating...");
			for (Location location : locations) {
				Element node = xmldoc.createElement("row");
				node.setAttribute("si", String.valueOf(location.getStationID()));
				node.setAttribute("s", location.getStationName());
				node.setAttribute("syi", String.valueOf(location.getSystemID()));
				node.setAttribute("sy", location.getSystemName());
				node.setAttribute("ci", String.valueOf(location.getConstellationID()));
				node.setAttribute("c", location.getConstellationName());
				node.setAttribute("ri", String.valueOf(location.getRegionID()));
				node.setAttribute("r", location.getRegionName());
				node.setAttribute("se", roundSecurity(location.getSecurity()));
				parentNode.appendChild(node);
			}
			return true;
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
		return false;
	}

	public static List<Location> updateSpecialSystems(Map<Integer, Location> systems) {
		//Systems
		List<EsiUpdater.Update<SystemResponse>> systemUpdates = new ArrayList<>();
		for (Integer systemID : systems.keySet()) {
			systemUpdates.add(new EsiUpdater.Update<SystemResponse>() {
				@Override
				public ApiResponse<SystemResponse> update() throws ApiException {
					return UNIVERSE_API.getUniverseSystemsSystemIdWithHttpInfo(systemID, null, DATASOURCE, null, null);
				}
			});
		}
		List<SystemResponse> systemResponses = EsiUpdater.update(systemUpdates);
		Set<Integer> stationIDs = new HashSet<>();
		for (SystemResponse response : systemResponses) {
			stationIDs.addAll(response.getStations());
		}
		//Stations
		List<EsiUpdater.Update<StationResponse>> stationUpdates = new ArrayList<>();
		for (Integer stationID : stationIDs) {
			stationUpdates.add(new EsiUpdater.Update<StationResponse>() {
				@Override
				public ApiResponse<StationResponse> update() throws ApiException {
					return UNIVERSE_API.getUniverseStationsStationIdWithHttpInfo(stationID, DATASOURCE, null);
				}
			});
		}
		List<StationResponse> stationResponses = EsiUpdater.update(stationUpdates);
		List<Location> locations = new ArrayList<>();
		for (StationResponse response : stationResponses) {
			Location system = systems.get(response.getSystemId());
			Location location = new Location(response.getStationId(), response.getName(), system.getSystemID(), system.getSystemName(), system.getConstellationID(), system.getConstellationName(), system.getRegionID(), system.getRegionName(), system.getSecurity());
			locations.add(location);
		}
		return locations;
	}

	private String roundSecurity(double number) {
		if (number < 0) { //0.0
			number = 0;
		} else if (number >= 0 && number <= 0.05) { //0.1
			number = number * 10;
			number = Math.ceil(number);
			number = number / 10;
		} else { //0.2 - 1.0
			number = number * 10;
			number = Math.round(number);
			number = number / 10;
		}
		return securityformater.format(number);
	}
}
