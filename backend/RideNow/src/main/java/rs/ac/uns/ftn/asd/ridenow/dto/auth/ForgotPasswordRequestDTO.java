package rs.ac.uns.ftn.asd.ridenow.dto.auth;

public class ForgotPasswordRequestDTO {
    private String email;

    public ForgotPasswordRequestDTO() {
        super();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
