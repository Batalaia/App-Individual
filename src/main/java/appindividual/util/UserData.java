package appindividual.util;

import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class UserData {
    public String username;
    public String password;
    public String confPassword;
    public String email;
    public String name;
    public String phone;
    public String workplace;
    public String address;
    public String occupation;
    public String NIF;
    public String privacy;
    public String activity;

    public UserData() {}

    public UserData(String username, String email, String name, String password, String confirmation,
                    String privacy, String activity, String phone, String workplace, String address, String occupation,
                    String NIF) {
        this.username = username;
        this.password = password;
        this.confPassword = confirmation;
        this.email=email;
        this.name = name;
        this.privacy=privacy;
        this.activity = activity;
        this.phone = phone;
        this.workplace = workplace;
        this.address = address;
        this.occupation= occupation;
        this.NIF = NIF;
    }

    public boolean nullComp() {
        return checkNull(username) || checkNull(password) || checkNull(confPassword) || checkNull(email) || checkNull(name);
    }

    public boolean checkNull(String word) {
        return word == "" || word == null;
    }

    public Response validData() {
        if(nullComp()) return Response.status(Status.BAD_REQUEST).entity("Please fill all the inputs.").build();
        Pattern specialChars = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern upperCaseChars = Pattern.compile("[A-Z ]");
        Pattern digitChars = Pattern.compile("[0-9 ]");
        if(!password.equals(confPassword))
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Passwords do not match").build();
        if(password.length() < 7)
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password length must be at least 7 characters.").build();
        if(!specialChars.matcher(password).find())
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password must have at least 1 special character.").build();
        if(!upperCaseChars.matcher(password).find())
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password must have at least 1 upper case character.").build();
        if(!digitChars.matcher(password).find())
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Password must have at least 1 digit character").build();
        String[] emailCheck = email.split("@");
        if(emailCheck.length != 2 || emailCheck[1] == "" || emailCheck[1].split(".").length < 2)
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity("Email must be in format <String>@<String>...<dom>.").build();
        return Response.ok().entity("Data is valid.").build();
    }
}
