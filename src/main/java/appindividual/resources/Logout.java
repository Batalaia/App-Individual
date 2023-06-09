package appindividual.resources;

import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class Logout {

    private static final Logger LOG = Logger.getLogger(Logout.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("LoginTokens");

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request) {
        String id = request.getHeader("Authorization");
        Transaction txn = datastore.newTransaction();
        try {
            id = id.substring("Bearer".length()).trim();
            Key tokenKey = tokenKeyFactory.newKey(id);
            Entity token = txn.get(tokenKey);
            if (token == null) {
                txn.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity("Error: Try again later").build();
            }
            String username = token.getString("username");
            txn.delete(tokenKey);
            txn.commit();
            LOG.fine("User logged out: " + username);
            return Response.ok().build();
        } catch (Exception e) {
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }
}
