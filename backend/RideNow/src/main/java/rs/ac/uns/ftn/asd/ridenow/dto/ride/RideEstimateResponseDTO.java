package rs.ac.uns.ftn.asd.ridenow.dto.ride;

public class RideEstimateResponseDTO {

    private int estimatedDurationMin;

    public RideEstimateResponseDTO(){
        super();
    }

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }
}