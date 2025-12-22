package rs.ac.uns.ftn.asd.ridenow.dto.auth;

public class ResetPasswordRequestDTO {
    private String newPassword;
    private  String confirmNewPassword;

    public ResetPasswordRequestDTO() {
        super();
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
