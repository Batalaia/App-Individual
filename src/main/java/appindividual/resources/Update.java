package appindividual.resources;

import com.google.cloud.datastore.*;

import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.logging.Logger;
import java.util.regex.Pattern;

@Path("/update")
public class Update {   

    private static final Logger LOG = Logger.getLogger(Update.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("LoginTokens");
    public Update() {
    }

    @PUT
    @Path("/updateUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@Context HttpServletRequest request) {
        String id = request.getHeader("Authorization");
        id = id.substring("Bearer".length()).trim();
        String username = request.getHeader("Username");
        String property = request.getHeader("Property");
        String change = request.getHeader("Change");
        Transaction txn = datastore.newTransaction();
        try{
            Key tokenKey = tokenKeyFactory.newKey(id);
            Entity token = datastore.get(tokenKey);
            if(token == null) {
                txn.rollback();
                return Response.ok(Status.BAD_REQUEST).entity("Not a valid Login.").build();
            }
            Entity userToUpdate = datastore.get(datastore.newKeyFactory().setKind("User").newKey(username));
            if(userToUpdate == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Error: Try again later").build();
            }
            LOG.fine("Attempt to update user: " + username);
            if(property.equals("username") || property.equals("password") || property.equals("photo")) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("Cannot change property").build();
            }
            if(property.equals("active") || property.equals("public"))
                if(!change.equals("true") && !change.equals("false"))
                    return Response.status(Status.BAD_REQUEST).entity("Wrong option, only valid options are true or false.").build();
            if(property.equals("role"))
                if(!change.equals("USER") && !change.equals("GBO") && !change.equals("GS"))
                    return Response.status(Status.BAD_REQUEST).entity("Wrong option, only valid options are USER, GBO or GS.").build();
                else if(token.getString("role") != "SU")
                    return Response.status(Status.BAD_REQUEST).entity("Don't have permissions").build();
            String delRole = userToUpdate.getString("role");
            switch (token.getString("role")) {
                case "USER":
                    if(!username.equals(token.getString("username")) || property.equals("role"))
                        return Response.status(Status.BAD_REQUEST).entity("Don't have permissions").build();
                    break;
                case "GBO":
                    if(!delRole.equals("USER") && !username.equals(token.getString("username")))
                        return Response.status(Status.BAD_REQUEST).entity("Error: Don't have permissions").build();
                    break;
                case "GS":
                    if(!username.equals(token.getString("username")) && !delRole.equals("USER") && !delRole.equals("GBO"))
                        return Response.status(Status.BAD_REQUEST).entity("Error: Don't have permissions").build();
                    break;
                case "SU":
                    if(!username.equals(token.getString("username")) && delRole.equals("SU"))
                        return Response.status(Status.BAD_REQUEST).entity("Error: Don't have permissions").build();
                    break;
                default:
                    return Response.status(Status.BAD_REQUEST).entity("Error: Try again later").build();
            }
            userToUpdate = Entity.newBuilder(userToUpdate)
                .set(property, change)
                .build();
            txn.update(userToUpdate);
            txn.commit();
            LOG.fine("User updated: " + username);
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

    private Response validPwd(String password, String confirmation) {
        Pattern specialChars = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern upperCase = Pattern.compile("[A-Z ]");
        Pattern digitCase = Pattern.compile("[0-9 ]");
        if(!password.equals(confirmation))
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Passwords do not match").build();
        if(password.length() < 7)
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password length must be at least 7 characters").build();
        if(!specialChars.matcher(password).find())
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password must have at least 1 special character").build();
        if(!upperCase.matcher(password).find())
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password must have at least 1 upper case character").build();
        if(!digitCase.matcher(password).find())
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password must have at least 1 digit character").build();
        return Response.ok().entity("Password is valid").build();
    }

    @PUT
    @Path("/updatePwd")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response updatePwd(@Context HttpServletRequest request) {
        String id = request.getHeader("Authorization");
        id = id.substring("Bearer".length()).trim();
        String password = request.getHeader("Password");
        String newPwd = request.getHeader("NewPwd");
        String confirmation = request.getHeader("Confirmation");
        Transaction txn = datastore.newTransaction();
        try {
            Key tokenKey = tokenKeyFactory.newKey(id);
            Entity token = datastore.get(tokenKey);
            if(token == null) {
                txn.rollback();
                return Response.ok(Status.BAD_REQUEST).entity("Not a valid Login.").build();
            }
            Entity user = datastore.get(datastore.newKeyFactory().setKind("User").newKey(token.getString("username")));
            if(user == null) {
                txn.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity("Error: Try again later").build();
            }

            if (!user.getString("password").equals(DigestUtils.sha512Hex(password)))
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Error: Wrong password").build();

            Response pwdValidation = validPwd(newPwd, confirmation);
            if(pwdValidation.getStatus() != Response.Status.OK.getStatusCode())
                return pwdValidation;

            user = Entity.newBuilder(user)
                .set("password", DigestUtils.sha512Hex(newPwd))
                .build();
            txn.update(user);
            txn.commit();
            return Response.ok(Status.OK).build();
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
