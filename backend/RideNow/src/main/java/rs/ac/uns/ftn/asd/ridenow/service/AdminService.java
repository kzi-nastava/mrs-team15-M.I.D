package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.AdminChangesReviewRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.DriverChangeRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    public List<DriverChangeRequestDTO> getDriverRequests() {
        List<DriverChangeRequestDTO> requests = new ArrayList<>();

        DriverChangeRequestDTO request = new DriverChangeRequestDTO();
        request.setEmail("driver@mail.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("123-456-7890");
        request.setProfileImage("profile_image_url");
        request.setAddress("123 Main St, Cityville");
        request.setLicensePlate("NS123AB");
        request.setVehicleModel("Toyota Prius");
        request.setVehicleType(VehicleType.STANDARD);
        request.setNumberOfSeats(4);
        request.setBabyFriendly(true);
        request.setPetFriendly(false);
        request.setStatus(DriverChangesStatus.PENDING);

        requests.add(request);

        return requests;
    }

    public void reviewDriverRequest(
            Long adminId,
            Long requestId,
            AdminChangesReviewRequestDTO dto) {

        if (dto.isApproved()) {
            // mock approve logic
            System.out.println(
                    "Admin " + adminId + " approved driver request " + requestId
            );
        } else {
            // mock reject logic
            System.out.println(
                    "Admin " + adminId + " rejected driver request " + requestId +
                            ". Reason: " + dto.getMessage()
            );
        }
    }
}
