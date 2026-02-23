package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ReportResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.model.Passenger;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RegisteredUserRepository;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final RideRepository rideRepository;
    private final RegisteredUserRepository registeredUserRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService authService,
                       RideRepository rideRepository, RegisteredUserRepository registeredUserRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.rideRepository = rideRepository;
        this.registeredUserRepository = registeredUserRepository;
    }

    public void changePassword(Long userId, ChangePasswordRequestDTO dto) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        User user = opt.get();

        // verify current password
        String stored = user.getPassword();
        boolean matches;
        matches = passwordEncoder.matches(dto.getCurrentPassword(), stored);
        if (!matches) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // validate new password confirmation
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // length check (model enforces @Size(min = 6) on User.password)
        if (dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }

        String hashed = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(hashed);
        userRepository.save(user);
    }

    public UserResponseDTO getUser(User user) {
        // Making UserResponse object from User object
        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAddress(user.getAddress());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImage(user.getProfileImage());
        dto.setActive(user.isActive());
        dto.setBlocked(user.isBlocked());

        if (user instanceof Driver driver) {
            Vehicle vehicle = driver.getVehicle();

            if (vehicle != null) {
                dto.setLicensePlate(vehicle.getLicencePlate());
                System.out.println(dto.getLicensePlate());
                dto.setVehicleModel(vehicle.getModel());
                dto.setVehicleType(vehicle.getType());
                dto.setNumberOfSeats(vehicle.getSeatCount());
                dto.setBabyFriendly(vehicle.isChildFriendly());
                dto.setPetFriendly(vehicle.isPetFriendly());
            }

            dto.setHoursWorkedLast24(driver.getWorkingHoursLast24());
        }

        return dto;
    }

    public void updateUser(Long userId, UpdateProfileRequestDTO dto, MultipartFile profileImage) throws IOException {

        Optional<User> opt = userRepository.findById(userId);
        // user does not exist
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        User user = opt.get();

        // check email uniqueness (if changed)
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            Optional<User> existing = userRepository.findByEmail(dto.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
            }
        }

        // update user fields
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        // Check if profile image is provided
        if (profileImage != null && !profileImage.isEmpty()){
            String profileImageURL = authService.generateProfileImageUrl(profileImage);
            user.setProfileImage(profileImageURL);
        }else{
            user.setProfileImage(user.getProfileImage());
        }

        userRepository.save(user);
    }

    public UserResponseDTO getUserById(Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        User user = opt.get();
        return getUser(user);
    }

    public Page<UserResponseDTO> getUsers(String search, String sortBy, String sortDirection, int page, int size) {
        // If no search and simple sort, leverage repository pageable
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isBlank()) {
            Sort.Direction d = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
            // Map field names for entity where needed
            String prop = switch (sortBy) {
                case "firstName" -> "firstName";
                case "lastName" -> "lastName";
                case "email" -> "email";
                case "role" -> "role";
                default -> sortBy;
            };
            sort = Sort.by(d, prop);
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        List<User> usersList;
        long totalElements;

        if (search == null || search.isBlank()) {
            var pageRes = userRepository.findAll(pageable);
            usersList = pageRes.getContent();
            totalElements = pageRes.getTotalElements();
        } else {
            // fallback: filter in-memory then page
            String q = search.trim().toLowerCase();
            List<User> filtered = userRepository.findAll().stream().filter(u -> {
                if (u.getRole() != null && u.getRole().toString().toLowerCase().contains(q)) return true;
                if (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) return true;
                if (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(q)) return true;
                if (u.getLastName() != null && u.getLastName().toLowerCase().contains(q)) return true;
                if (u.getPhoneNumber() != null && u.getPhoneNumber().toLowerCase().contains(q)) return true;
                return false;
            }).collect(java.util.stream.Collectors.toList());
            // apply sort if requested
            if (sort.isSorted()) {
                java.util.Comparator<User> comp = (a, b) -> 0;
                if ("blocked".equals(sortBy)) {
                    comp = java.util.Comparator.comparing(User::isBlocked);
                } else if ("email".equals(sortBy)) {
                    comp = java.util.Comparator.comparing(u -> u.getEmail() == null ? "" : u.getEmail(), String.CASE_INSENSITIVE_ORDER);
                } else if ("firstName".equals(sortBy)) {
                    comp = java.util.Comparator.comparing(u -> u.getFirstName() == null ? "" : u.getFirstName(), String.CASE_INSENSITIVE_ORDER);
                } else if ("lastName".equals(sortBy)) {
                    comp = java.util.Comparator.comparing(u -> u.getLastName() == null ? "" : u.getLastName(), String.CASE_INSENSITIVE_ORDER);
                } else if ("role".equals(sortBy)) {
                    comp = java.util.Comparator.comparing(u -> u.getRole() == null ? "" : u.getRole().toString(), String.CASE_INSENSITIVE_ORDER);
                }
                if (sort.isSorted() && comp != null) {
                    if (sort.stream().findFirst().get().isDescending()) comp = comp.reversed();
                    filtered.sort(comp);
                }
            }
            totalElements = filtered.size();
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filtered.size());
            if (start > end) usersList = List.of(); else usersList = filtered.subList(start, end);
        }

        List<UserResponseDTO> content = new ArrayList<>();
        for (User user : usersList) content.add(getUser(user));

        return new PageImpl<>(content, pageable, totalElements);
    }

    public Void blockUser(Long id) {
        // find user and set blocked
        User user = userRepository.findById(id).get();

        user.setBlocked(true);
        userRepository.save(user);
        return null;
    }

    public Void unblockUser(Long id) {
        // find user and delete block
        User user = userRepository.findById(id).get();

        user.setBlocked(false);
        userRepository.save(user);
        return null;
    }

    public ReportResponseDTO getReport(@Min(0) Long startDate, @Min(0) Long endDate, Long id) {
        ReportResponseDTO response = new ReportResponseDTO();

        // load user
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        User user = optUser.get();

        // prepare list of all relevant rides (status FINISHED or CANCELLED) for determining defaults
        List<Ride> allRelevantRides = new ArrayList<>();
        if (user instanceof Driver) {
            Driver driver = (Driver) user;
            List<Ride> driverRides = rideRepository.findByDriver(driver);
            allRelevantRides = driverRides.stream()
                    .filter(r -> r.getScheduledTime() != null && r.getStatus() != null)
                    .filter(r -> {
                        String s = r.getStatus().toString();
                        return "FINISHED".equals(s) || "CANCELLED".equals(s);
                    })
                    .collect(Collectors.toList());
        } else {
            RegisteredUser reg = registeredUserRepository.findById(id).orElse(null);
            if (reg != null) {
                for (Passenger p : reg.getRideParticipation()) {
                    Ride r = p.getRide();
                    if (r == null) continue;
                    if (r.getScheduledTime() == null || r.getStatus() == null) continue;
                    String s = r.getStatus().toString();
                    if ("FINISHED".equals(s) || "CANCELLED".equals(s)) {
                        allRelevantRides.add(r);
                    }
                }
            }
        }

        // Determine date range: default end = now, default start = date of oldest relevant ride (if exists) or today
        LocalDateTime defaultEnd = LocalDateTime.now();
        LocalDateTime end = (endDate == null) ? defaultEnd : LocalDateTime.ofInstant(Instant.ofEpochMilli(endDate), ZoneId.systemDefault());

        LocalDateTime defaultStart;
        if (!allRelevantRides.isEmpty()) {
            // find earliest scheduledTime
            LocalDateTime earliest = allRelevantRides.stream().map(Ride::getScheduledTime).min(LocalDateTime::compareTo).orElse(defaultEnd);
            defaultStart = earliest;
        } else {
            // no rides - default to today
            defaultStart = defaultEnd;
        }
        LocalDateTime start = (startDate == null) ? defaultStart : LocalDateTime.ofInstant(Instant.ofEpochMilli(startDate), ZoneId.systemDefault());

        // validate range
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        // prepare per-day maps
        Map<LocalDate, Integer> ridesPerDay = new HashMap<>();
        Map<LocalDate, Double> kmPerDay = new HashMap<>();
        Map<LocalDate, Double> moneyPerDay = new HashMap<>();

        double totalKm = 0.0;
        double totalMoney = 0.0;
        int totalRides = 0;

        // Now filter rides to the requested/derived date range
        List<Ride> rides = allRelevantRides.stream()
                .filter(r -> {
                    LocalDateTime st = r.getScheduledTime();
                    if (st == null) return false;
                    return (!st.isBefore(start)) && (!st.isAfter(end));
                })
                .collect(Collectors.toList());

        // Aggregate by day of scheduledTime
        for (Ride r : rides) {
            LocalDateTime st = r.getScheduledTime();
            if (st == null) continue;
            LocalDate day = st.toLocalDate();

            // rides count
            ridesPerDay.put(day, ridesPerDay.getOrDefault(day, 0) + 1);
            totalRides++;

            // km
            double km = r.getDistanceKm();
            kmPerDay.put(day, kmPerDay.getOrDefault(day, 0.0) + km);
            totalKm += km;

            // money: passengers spent money only if they are the creator of the ride
            double money = 0.0;
            if (user instanceof Driver) {
                money = r.getPrice();
            } else {
                for (Passenger p : r.getPassengers()) {
                    if (p.getUser() != null && p.getUser().getId() != null && p.getUser().getId().equals(id)) {
                        try {
                            if (p.getRole() != null && p.getRole().toString().equals("CREATOR")) {
                                money = r.getPrice();
                            }
                        } catch (Exception ex) {
                            // ignore
                        }
                        break;
                    }
                }
            }
            moneyPerDay.put(day, moneyPerDay.getOrDefault(day, 0.0) + money);
            totalMoney += money;
        }

        // compute averages over all calendar days in the inclusive range
        long daysInRange = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (daysInRange <= 0) daysInRange = 1;

        response.setRidesPerDay(ridesPerDay);
        response.setKmPerDay(kmPerDay);
        response.setMoneyPerDay(moneyPerDay);

        response.setSumRides(totalRides);
        response.setSumKM(totalKm);
        response.setSumMoney(totalMoney);

        response.setAvgRides((int) Math.round((double) totalRides / (double) daysInRange));
        response.setAvgKM(totalKm / (double) daysInRange);
        response.setAvgMoney(totalMoney / (double) daysInRange);

        response.setStartDate(LocalDate.from(start));
        response.setEndDate(LocalDate.from(end));
        return response;
    }
}
