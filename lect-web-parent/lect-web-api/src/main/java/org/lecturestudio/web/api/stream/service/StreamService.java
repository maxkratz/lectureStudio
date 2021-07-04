/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.stream.service;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.client.TokenProvider;
import org.lecturestudio.web.api.service.ProviderService;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.client.StreamRestClient;
import org.lecturestudio.web.api.stream.model.Lecture;

/**
 * Service implementation to manage streaming related information with streaming
 * servers. As of the current implementation this service will communicate with
 * a REST API providing a WebRTC streaming interface.
 *
 * @author Alex Andres
 */
public class StreamService extends ProviderService {

	private final StreamRestClient streamRestClient;


	/**
	 * Creates a new {@code StreamService}.
	 *
	 * @param parameters    The service connection parameters.
	 * @param tokenProvider The access token provider.
	 */
	@Inject
	public StreamService(ServiceParameters parameters, TokenProvider tokenProvider) {
		RestClientBuilder builder = createClientBuilder(parameters);
		builder.property(TokenProvider.class.getName(), tokenProvider);

		streamRestClient = builder.build(StreamRestClient.class);
	}

	/**
	 * Gets a list of all lectures associated with a user. The user must be
	 * authenticated with a token to the API.
	 *
	 * @return A list of all lectures associated with a user.
	 */
	public List<Lecture> getLectures() {
		return streamRestClient.getLectures();
	}
}
