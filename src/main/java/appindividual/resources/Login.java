package appindividual.resources;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

import appindividual.util.AuthToken;
import appindividual.util.LoginData;


@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class Login {
    /**
     * Logger Object
     */
    private static final Logger LOG = Logger.getLogger(Login.class.getName());

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public Login() {}

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response login(LoginData data, @Context HttpServletRequest request, @Context HttpHeaders headers) {
        LOG.fine("Attempt to login user:" + data.username);
        if (data.nullComp()) return Response.status(Status.BAD_REQUEST).build();
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        Entity user = datastore.get(userKey);
        if(user == null) return Response.status(Status.NOT_FOUND).build();
        if(!DigestUtils.sha512Hex(data.password).equals(user.getString("password")))
            return Response.status(Status.BAD_REQUEST).entity("Wrong Password.").build();
        AuthToken token;
        Key tknsKey;
        do {
            token = new AuthToken(data.username);
            tknsKey = datastore.newKeyFactory().addAncestor(PathElement.of("User", data.username))
                                .setKind("UserTokens").newKey(token.tokenID);
        }
        while(datastore.get(tknsKey) != null);

        Entity uTokens = Entity.newBuilder(tknsKey)
        .set("username", token.username)
        .set("creation_data", token.creationData)
        .set("expiration_data", token.expirationData)
        .set("role", "USER")
        .build();
        datastore.put(uTokens);
        LOG.fine("User: " + data.username + " Logged in.");
        return Response.ok(new AuthToken(data.username)).build();
    }
}
