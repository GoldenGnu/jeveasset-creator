
package net.nikr.eve.io.data.agents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NpcCharacter {
	private int corporationID;
	private long locationID;
	private Map<String, String> name;
	private Agent agent;

	public String getEnglishName() {
		return name.get("en");
	}

	public int getCorporationID() {
		return corporationID;
	}

	public long getLocationID() {
		return locationID;
	}

	public Agent getAgent() {
		return agent;
	}

	public Map<String, String> getName() {
		return name;
	}
}
