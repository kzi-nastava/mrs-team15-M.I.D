package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.*;
import jakarta.validation.Valid;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.service.AdminService;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    public AdminController(AdminService adminService, UserService userService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all-users")
    public ResponseEntity<Page<UserItemDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), mapSortField(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserItemDTO> users = adminService.getNonAdminUsers(pageable);
        return ResponseEntity.ok(users);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "surname" -> "lastName";
            case "role" -> "role";
            case "email" -> "email";
            default -> "firstName";
        };
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ride-history")
    public ResponseEntity<Page<AdminRideHistoryItemDTO>> getRideHistory(@RequestParam Long id, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(defaultValue = "date") String sortBy, @RequestParam(defaultValue = "desc") String sortDir,
                                                                   @RequestParam(required = false) @Min(0) Long date) {

        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        User user = optionalUser.get();
        Page<AdminRideHistoryItemDTO> history;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), mapHistorySortField(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        history = adminService.getRideHistory(user, pageable, date);
        return ResponseEntity.ok(history);
    }

    private String mapHistorySortField(String sortBy) {
        return switch (sortBy) {
            case "route" -> "route.startLocation.address";
            case "startTime" -> "startTime";
            case "endTime" -> "endTime";
            case "cancelled" -> "cancelled";
            case "price" -> "price";
            case "panic" -> "panicAlert";
            default -> "scheduledTime";
        };
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/driver-register")
    public ResponseEntity<RegisterDriverResponseDTO> register(
            @Valid @RequestBody RegisterDriverRequestDTO request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(201).body(adminService.register(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/driver-requests")
    public ResponseEntity<List<DriverChangeRequestDTO>> getDriverRequests() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(adminService.getDriverRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("driver-requests/{requestId}")
    public ResponseEntity<Void> reviewDriverRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody AdminChangesReviewRequestDTO dto) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();
        adminService.reviewDriverRequest(id, requestId, dto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getUsers(search, sortBy, sortDirection, page, size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block/{id}")
    public ResponseEntity<Void> blockUser(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/unblock/{id}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.unblockUser(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/price-configs")
    public ResponseEntity<PriceConfigResponseDTO> getPriceConfigs() {
        return ResponseEntity.ok(adminService.getPriceConfigs());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/price-configs")
    public ResponseEntity<Void> updatePriceConfigs(@Valid @RequestBody PriceConfigRequestDTO request) {
        adminService.updatePriceConfigs(request);
        return ResponseEntity.ok().build();
    }
}
