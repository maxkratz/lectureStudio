/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.app.configuration.bind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Calendar;

/**
 * Implementation of a {@link Calendar} JSON deserializer.
 *
 * @author Alex Andres
 */
public class CalendarDeserializer extends JsonDeserializer<Calendar> {

	@Override
	public Calendar deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		JsonNode node = parser.getCodec().readTree(parser);

		if (!node.canConvertToLong()) {
			throw new IOException("Deserialize calendar failed.");
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(node.longValue());

		return calendar;
	}

}
