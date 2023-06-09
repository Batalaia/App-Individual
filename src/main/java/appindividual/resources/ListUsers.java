package appindividual.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@Path("/list")
public class ListUsers {
    private static final Logger LOG = Logger.getLogger(ListUsers.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("LoginTokens");
    private final Gson g = new Gson();
    public ListUsers() {
    }

    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listUsers(@Context HttpServletRequest request) {
        String id = request.getHeader("Authorization");
        LOG.fine("Attempt to list users");
        Transaction txn = datastore.newTransaction();
        try {
            id = id.substring("Bearer".length()).trim();
            Key tokenKey = tokenKeyFactory.newKey(id);
            Entity token = txn.get(tokenKey);
            if(token == null)
                return Response.status(Status.BAD_REQUEST).entity("Error: not logged in.").build();
                
            Query<Entity> query;
            QueryResults<Entity> results;
            List<Entity> list;
            switch (token.getString("role")) {
                case "USER":
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(
                                    CompositeFilter.and(
                                            PropertyFilter.eq("role", "USER"),
                                            PropertyFilter.eq("active", "true"),
                                            PropertyFilter.eq("public", "true")
                                    )
                            ).build();
                    break;
                case "GBO":
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(
                                    PropertyFilter.eq("role", "USER")
                            ).build();
                    break;
                case "GS":
                    List<Value<String>> l = new ArrayList<>();
                    l.add(StringValue.of("USER"));
                    l.add(StringValue.of("GBO"));

                    ListValue listValue = ListValue.newBuilder().set(l)
                            .build();
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(
                                    PropertyFilter.in("role", listValue)
                            ).build();
                    break;
                case "SU":
                    query = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .build();
                    break;
                default:
                    return Response.status(Status.BAD_REQUEST).entity("Error: Try again later").build();
            }
            results = datastore.run(query);
            list = new ArrayList<>();
            results.forEachRemaining(list::add);
            return Response.ok(g.toJson(list)).build();
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

    @GET
    @Path("/profile")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getProfile(@Context HttpServletRequest request) {
        String id = request.getHeader("Authorization");
        id = id.substring("Bearer".length()).trim();
        Key tokenKey = tokenKeyFactory.newKey(id);
        Entity token = datastore.get(tokenKey);
        if(token == null)
            return Response.status(Status.BAD_REQUEST).entity("Error: not logged in.").build();
        Entity user = datastore.get(datastore.newKeyFactory().setKind("User").newKey(token.getString("username")));
        JsonObject userJson = g.toJsonTree(user).getAsJsonObject();
        return Response.ok(g.toJson(userJson)).build();
    }
}