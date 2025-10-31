package net.nikr.eve.io.yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.nikr.eve.io.data.flag.Flag;

public class FlagsReader {

	public List<Flag> loadFlags() throws IOException {
		List<String> enumValues = EsiOpenApiParser.getLocationFlagEnumValues();
		List<Flag> flags = new ArrayList<>();

		int flagID = 0;
		for (String enumValue : enumValues) {
			Flag flag = new Flag();
			flag.setFlagID(flagID++);
			flag.setFlagName(enumValue);
			flag.setFlagText(formatFlagText(enumValue));
			flags.add(flag);
		}

		return flags;
	}

	private String formatFlagText(String enumValue) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < enumValue.length(); i++) {
			char c = enumValue.charAt(i);
			if (i > 0 && Character.isUpperCase(c)) {
				result.append(' ');
			}
			result.append(c);
		}
		return result.toString();
	}
}
