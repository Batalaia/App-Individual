package appindividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Entity;

import appindividual.util.UserData;

@Path("/register")
public class Register {

    private static final Logger LOG = Logger.getLogger(Register.class.getName()); 
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Register() {}
    
    @POST
    @Path("/{role}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registUser(UserData user, @PathParam ("role") String role) {
        
        LOG.fine("Attempting to register user " + user.username);

        Response valid = user.validData();
        if(valid.getStatus() != Status.OK.getStatusCode()) return valid;
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(user.username);
            Entity newUser = txn.get(userKey);
            if(newUser != null) {
                txn.rollback();
                 return Response.status(Status.CONFLICT).entity("User already exists.").build();
            }
            else {
                newUser = Entity.newBuilder(userKey)
                            .set("password", DigestUtils.sha512Hex(user.password))
                            .set("email", user.email)
                            .set("name", user.name)
                            .set("role", role)
                            .set("active", false)
                            .set("public", false).build();
                txn.add(newUser);
                LOG.info("User " + user.username + " Registered.");
                txn.commit();
                return Response.ok(Status.OK).build();
            }
        } finally {
            if(txn.isActive()) txn.rollback();
        }
    }
}
