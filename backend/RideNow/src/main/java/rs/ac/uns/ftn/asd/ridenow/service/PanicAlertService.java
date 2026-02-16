package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.UserItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.PanicAlertDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RoutePointDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.PanicAlertRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.websocket.NotificationWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PanicAlertService {
    @Autowired
    private PanicAlertRepository panicAlertRepository;

    @Autowired
    private RideRepository rideRepository;

    private final NotificationWebSocketHandler webSocketHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public PanicAlertService(@Lazy NotificationWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Transactional
    public PanicAlertDTO triggerPanicAlert(User user) throws Exception {
        Optional<Ride> optionalRide = findCurrentRide(user);

        if(optionalRide.isEmpty()){
            throw new Exception("You don't have ride in progress");
        }
        Ride ride = optionalRide.get();
        if(ride.getPanicAlert() != null  && !ride.getPanicAlert().isResolved()){
            throw new Exception("Panic mode already active. Help is on the way!");
        }
        PanicAlert panicAlert = new PanicAlert();
        panicAlert.setRide(ride);
        panicAlert.setResolved(false);
        panicAlert.setPanicBy(user.getEmail());
        panicAlert.setPanicByRole(user.getRole().name());
        panicAlert = panicAlertRepository.save(panicAlert);

        Map<String, Object> panicData = new HashMap<>();
        panicData.put("rideId", ride.getId());
        panicData.put("triggeredBy", panicAlert.getPanicByRole());
        panicData.put("triggeredByUserId", panicAlert.getPanicBy());
        panicData.put("timestamp", new Date());

        ride.setPanicAlert(panicAlert);
        rideRepository.save(ride);

        PanicAlertDTO dto = convertToDTO(panicAlert, ride);
        webSocketHandler.broadcastNewPanic(dto);
        webSocketHandler.broadcastRidePanic(ride.getId(), panicData);
        System.out.println("Panic alert created and broadcast for ride #" + ride.getId());
        System.out.println("Panic alert triggered for ride " + ride.getId() + " by " + panicAlert.getPanicByRole());
        return dto;
    }

    private Optional<Ride> findCurrentRide(User user) {
        if(user instanceof RegisteredUser registeredUser){
            return rideRepository.findCurrentRideByUser(registeredUser.getId());
        }
        else if(user instanceof  Driver driver){
            return rideRepository.findCurrentRideByDriver(driver.getId());
        }
        return Optional.empty();
    }

    private PanicAlertDTO convertToDTO(PanicAlert panicAlert, Ride ride) {
        PanicAlertDTO dto = new PanicAlertDTO();
        dto.setId(panicAlert.getId());
        dto.setRideId(ride.getId());
        dto.setPanicBy(panicAlert.getPanicBy());
        dto.setPanicByRole(panicAlert.getPanicByRole());
        dto.setCreatedAt(panicAlert.getCreatedAt().toString());
        dto.setResolved(panicAlert.isResolved());

        if(panicAlert.isResolved()){
            dto.setResolvedAt(panicAlert.getResolvedAt().toString());
            dto.setResolvedBy(panicAlert.getResolvedBy());

            if(panicAlert.getResolvedBy() != null){
               Optional<User> optionalAdmin =  userRepository.findById(panicAlert.getResolvedBy());
                optionalAdmin.ifPresent(admin -> dto.setResolvedByEmail(admin.getEmail()));
            }
        }

        if(ride.getDriver() != null){
            dto.setDriverEmail(ride.getDriver().getEmail());
        }

        if(ride.getPassengers() != null){
            for(Passenger passenger : ride.getPassengers()){
                if(passenger.getRole() == PassengerRole.CREATOR){
                    dto.setPassengerEmail(passenger.getUser().getEmail());
                }
            }
        }

        RoutePointDTO location = new RoutePointDTO();
        if (ride.getDriver() != null && ride.getDriver().getVehicle() != null) {
            location.setLat(ride.getDriver().getVehicle().getLat());
            location.setLng(ride.getDriver().getVehicle().getLon());
        } else {
            location.setLat(0.0);
            location.setLng(0.0);
        }
        dto.setLocation(location);
        return  dto;
    }

    public List<PanicAlertDTO> getAllUnresolvedAlerts() {
        List<PanicAlertDTO> result = new ArrayList<>();
        for (PanicAlert pa : panicAlertRepository.findAllUnresolved()) {
            result.add(convertToDTO(pa, pa.getRide()));
        }
        return result;
    }

    public Page<PanicAlertDTO> getUnresolvedAlerts(Pageable pageable) {
        Page<PanicAlert> alerts = panicAlertRepository.findByResolvedFalse(pageable);
        List<PanicAlertDTO> dtos = alerts.getContent().stream()
                .map(this::mapToPanicAlertDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, alerts.getTotalElements());
    }

    private PanicAlertDTO mapToPanicAlertDTO(PanicAlert panicAlert) {
        return convertToDTO(panicAlert, panicAlert.getRide());
    }

    public List<PanicAlertDTO> getAllAlerts(){
        List<PanicAlertDTO> result = new ArrayList<>();
        for(PanicAlert pa : panicAlertRepository.findAll()){
            result.add(convertToDTO(pa, pa.getRide()));
        }
        return result;
    }

    public PanicAlertDTO getAlertById(Long id) throws Exception {
        Optional<PanicAlert> optionalPanicAlert = panicAlertRepository.findById(id);
        if(optionalPanicAlert.isEmpty()){
            throw  new Exception("Panic alert not found");
        }
        PanicAlert panicAlert = optionalPanicAlert.get();
        return convertToDTO(panicAlert, panicAlert.getRide());
    }

    @Transactional
    public void resolvePanicAlert(Long panicAlertId, Long adminUserId) throws Exception {
        Optional<PanicAlert> optionalPanicAlert = panicAlertRepository.findByIdAndResolvedFalse(panicAlertId);
        if(optionalPanicAlert.isEmpty()){
            throw new Exception("Panic alert not found or already resolved");
        }
        PanicAlert panicAlert = optionalPanicAlert.get();
        panicAlert.setResolved(true);
        panicAlert.setResolvedAt(LocalDateTime.now());
        panicAlert.setResolvedBy(adminUserId);
        panicAlertRepository.save(panicAlert);

        // Broadcast resolution to all admins
        webSocketHandler.broadcastPanicResolved(panicAlertId);
        System.out.println("Panic alert #" + panicAlertId + " resolved by admin #" + adminUserId);
    }
}
