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

package net.nikr.eve.io.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EsiOpenApiParser {

	private final static Logger LOG = LoggerFactory.getLogger(EsiOpenApiParser.class);
	private final static String ESI_OPENAPI_URL = "https://esi.evetech.net/meta/openapi.json?compatibility_date=2025-09-30";

	public static List<String> getLocationFlagEnumValues() throws IOException {
		LOG.info("		Downloading ESI OpenAPI schema...");
		JsonNode schema;
		try (InputStream in = new URL(ESI_OPENAPI_URL).openStream()) {
			schema = YamlHelper.JSON.readTree(in);
		}

		LOG.info("		Parsing LocationFlag enum from schema...");
		JsonNode components = schema.get("components");
		if (components == null) {
			throw new IOException("Schema missing 'components' section");
		}

		JsonNode schemas = components.get("schemas");
		if (schemas == null) {
			throw new IOException("Schema missing 'components.schemas' section");
		}

		JsonNode assetsSchema = schemas.get("CharactersCharacterIdAssetsGet");
		if (assetsSchema == null) {
			throw new IOException("Schema missing 'CharactersCharacterIdAssetsGet' schema");
		}

		JsonNode items = assetsSchema.get("items");
		if (items == null) {
			throw new IOException("Schema missing 'items' in CharactersCharacterIdAssetsGet");
		}

		JsonNode properties = items.get("properties");
		if (properties == null) {
			throw new IOException("Schema missing 'properties' in items");
		}

		JsonNode locationFlag = properties.get("location_flag");
		if (locationFlag == null) {
			throw new IOException("Schema missing 'location_flag' property");
		}

		JsonNode enumValues = locationFlag.get("enum");
		if (enumValues == null || !enumValues.isArray()) {
			throw new IOException("Schema missing 'enum' array in location_flag");
		}

		List<String> flagNames = new ArrayList<>();
		for (JsonNode enumValue : enumValues) {
			if (enumValue.isTextual()) {
				flagNames.add(enumValue.asText());
			}
		}

		LOG.info("		Found " + flagNames.size() + " LocationFlag enum values");
		return flagNames;
	}
}

