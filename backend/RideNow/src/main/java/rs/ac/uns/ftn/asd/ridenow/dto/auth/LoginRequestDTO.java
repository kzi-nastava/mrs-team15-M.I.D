package rs.ac.uns.ftn.asd.ridenow.dto.auth;

public class LoginRequestDTO {
    private String email;
    private String password;

    public LoginRequestDTO() {
        super();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}