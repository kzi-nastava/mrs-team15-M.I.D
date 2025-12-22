package rs.ac.uns.ftn.asd.ridenow.dto.ride;

public class CancelRideRequestDTO {
    private String reason;

    public CancelRideRequestDTO() {
        super();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
