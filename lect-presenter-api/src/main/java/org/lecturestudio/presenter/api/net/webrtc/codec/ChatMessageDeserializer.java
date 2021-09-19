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

package org.lecturestudio.presenter.api.net.webrtc.codec;

import java.lang.reflect.Type;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import org.lecturestudio.presenter.api.model.ChatMessage;

public class ChatMessageDeserializer implements JsonbDeserializer<ChatMessage> {

	/**
	 * Used to prevent recursive calls to the deserializer.
	 */
	private final Jsonb jsonb;


	ChatMessageDeserializer() {
		jsonb = JsonbBuilder.create();
	}

	@Override
	public ChatMessage deserialize(JsonParser parser, DeserializationContext ctx, Type type) {
		if (parser.hasNext()) {
			// Skip START_OBJECT.
			parser.next();
			// Get class name key.
			parser.next();

			String className = parser.getString();

			// Move to the concrete object.
			parser.next();

			String object = parser.getObject().toString();

			try {
				return jsonb.fromJson(object, Class.forName(className).asSubclass(ChatMessage.class));
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

}