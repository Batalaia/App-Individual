package appindividual.util;

public class LoginData {

    public String username;
    public String password;
    public long timestamp;

    public LoginData() {}

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean nullComp() {
        return checkNull(username) || checkNull(password);
    }
    public boolean checkNull(String word) {
        return word == "" || word == null;
    }
}
