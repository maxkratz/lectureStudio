package org.lecturestudio.web.api.client;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.web.api.data.JsonbContextResolver;
import org.lecturestudio.web.api.filter.AuthorizationFilter;
import org.lecturestudio.web.api.filter.LoggingFilter;
import org.lecturestudio.web.api.model.JoinedRooms;
import org.lecturestudio.web.api.model.Room;


@Path("/_matrix/client/r0")
@RegisterProviders({
        @RegisterProvider(JsonbContextResolver.class),
        @RegisterProvider(AuthorizationFilter.class),
        @RegisterProvider(LoggingFilter.class),
})
public interface RoomService {

    public static ObjectProperty<Room> defaultRoom = new ObjectProperty<Room>();

    /**
     * Gets a list of the user's current rooms in which the user has {@code joined}
     * membership.
     *
     * @return A list of room IDs the user has joined.
     */
    @GET
    @Path("/joined_rooms")
    JoinedRooms getJoinedRooms();

    /**
     *
     * @param roomId
     * @return Name pf the given RoomID
     */
    @GET
    @Path("/rooms/{roomId}/state/m.room.name/")
    JoinedRooms getRoomAliases(@PathParam("roomId") String roomId);

}