package appindividual.resources;

import com.google.cloud.datastore.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.logging.Logger;

@Path("/updatePhoto")
public class Foto {

    private static final Logger LOG = Logger.getLogger(Foto.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("LoginTokens");

    public Foto() {}

    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@Context HttpServletRequest request) {
        String id = request.getHeader("Authorization");
        id = id.substring("Bearer".length()).trim();
        String photo = request.getHeader("photo");
        Transaction txn = datastore.newTransaction();
        try{
            Key tokenKey = tokenKeyFactory.newKey(id);
            Entity token = datastore.get(tokenKey);
            if(token == null) {
                txn.rollback();
                return Response.ok(Status.BAD_REQUEST).entity("Not a valid Login.").build();
            }
            Entity userToUpdate = datastore.get(datastore.newKeyFactory().setKind("User").newKey(token.getString("username")));
            userToUpdate = Entity.newBuilder(userToUpdate)
                            .set("photo", photo)
                            .build();
            datastore.put(userToUpdate);
            return Response.ok().build();
        } catch (Exception e) {
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
