package appindividual.resources;

import com.google.cloud.datastore.*;

import appindividual.filters.Secured;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.logging.Logger;

@Path("/delete")
public class Delete {
    private static final Logger LOG = Logger.getLogger(Delete.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");
    public Delete() {
    }

    @DELETE
    @Secured
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@Context HttpServletRequest request) {
        String id = request.getHeader("Authorization");
        String username = request.getHeader("Username");
        LOG.fine("Attempt to delete user: " + username);
        Transaction txn = datastore.newTransaction();
        try{
            id = id.substring("Bearer".length()).trim();
            Key tokenKey = tokenKeyFactory.newKey(id);
            Key delKey = datastore.newKeyFactory().setKind("User").newKey(username);
            Entity token = txn.get(tokenKey);
            Entity del = txn.get(delKey);

            String delRole = del.getString("role");
            switch (token.getString("role")){
                case "USER":
                    if(!token.getString("username").equals(username))
                        return Response.status(Status.BAD_REQUEST).entity("Error: Don't have permissions").build();
                    break;
                case "GBO":
                    if(!delRole.equals("USER"))
                        return Response.status(Status.BAD_REQUEST).entity("Error: Don't have permissions").build();
                    break;
                case "GS":
                    if(!delRole.equals("USER") && !delRole.equals("GBO"))
                        return Response.status(Status.BAD_REQUEST).entity("Error: Don't have permissions").build();
                    break;
                case "SU":
                    break;
                default:
                    return Response.status(Status.BAD_REQUEST).entity("Error: Don't have permissions").build();
            }
            txn.delete(delKey);
            txn.commit();
            LOG.fine("User deleted: " + username);
            return Response.ok().build();
        } catch (Exception e) {
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Status.FORBIDDEN).build();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

}