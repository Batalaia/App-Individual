package appindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.ProjectionEntity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
@Path("/list")
public class ListUsers {
    private static final Logger LOG = Logger.getLogger(Login.class.getName());

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public ListUsers() {}

    @POST
    @Path("/{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response list(@PathParam ("token") String tokenID) {
        LOG.fine("Attempt to list Users.");
        Key tokenKey = datastore.newKeyFactory().setKind("LoginTokens").newKey(tokenID);
        Entity token = datastore.get(tokenKey);
        if(token == null) return Response.ok(Status.BAD_REQUEST).entity("Not a valid Login.").build();
        String role = token.getString("role");
        if(role == "USER"){
            Query<ProjectionEntity> query = Query.newProjectionEntityQueryBuilder().setKind("User")
                        .setProjection("username", "name", "email")
                        .setFilter(CompositeFilter.and(PropertyFilter.eq("role", "USER")
                        , PropertyFilter.eq("public", true)
                        , PropertyFilter.eq("active", true)))
                        .build();
            QueryResults<ProjectionEntity> users = datastore.run(query);
            return Response.ok(users).build();
        }
        else if(role == "GBO") {
            Query<Entity> query = Query.newEntityQueryBuilder().setKind("User")
                        .setFilter(PropertyFilter.eq("role", "USER")).build();
            QueryResults<Entity> users = datastore.run(query);
            return Response.ok(users).build();
        }
        else if(role == "GS") {
            Query<Entity> query = Query.newEntityQueryBuilder().setKind("User")
                        .setFilter(PropertyFilter.eq("role", role)).build();
            QueryResults<Entity> users = datastore.run(query);
            return Response.ok(users).build();        
        }
        else {
            Query<Entity> query = Query.newEntityQueryBuilder().setKind("User")
                        .setFilter(PropertyFilter.eq("role", role)).build();
            QueryResults<Entity> users = datastore.run(query);
            return Response.ok(users).build();
        }
    }
}
