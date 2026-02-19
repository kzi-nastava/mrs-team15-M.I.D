package rs.ac.uns.ftn.asd.ridenow.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.*;
import jakarta.validation.Valid;
import rs.ac.uns.ftn.asd.ridenow.dto.user.BlockUserRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ReportResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.service.AdminService;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@Tag(name = "Admin", description = "Admin management endpoints")
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

    @Operation(summary = "Get all non-admin users", description = "Retrieve a paginated list of all non-admin users with sorting options")
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

    @Operation(summary = "Get user ride history", description = "Retrieve a paginated list of rides for a specific user with sorting and filtering options")
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

    @Operation(summary = "Register new driver", description = "Admin registers a new driver with profile information and optional profile image")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/driver-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @Valid @ModelAttribute RegisterDriverRequestDTO request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            RegisterDriverResponseDTO responseDTO = adminService.register(request, profileImage);
            return ResponseEntity.status(201).body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get driver change requests", description = "Retrieve a list of pending driver profile change requests for admin review")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/driver-requests")
    public ResponseEntity<List<DriverChangeRequestDTO>> getDriverRequests() {
        return ResponseEntity.ok(adminService.getDriverRequests());
    }

    @Operation(summary = "Review driver change request", description = "Admin reviews a driver profile change request and approves or rejects it with optional comments")
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

    @Operation(summary = "Get user profile by ID", description = "Retrieve user profile information by user ID for admin use")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Get users with search and sorting", description = "Retrieve a paginated list of users with optional search by name or email and sorting options for admin use")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsers(search, sortBy, sortDirection, page, size));
    }

    @Operation(summary = "Block user", description = "Admin blocks a user by ID, preventing them from ordering and taking rides")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/block/{id}")
    public ResponseEntity<Void> blockUser(@PathVariable Long id, @Valid @RequestBody BlockUserRequestDTO request) {
        return ResponseEntity.ok(userService.blockUser(id, request.getReason()));
    }

    @Operation(summary = "Unblock user", description = "Admin unblocks a user by ID, restoring their access to their account")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/unblock/{id}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }

    @Operation(summary = "Get price configurations", description = "Retrieve current price configurations for admin review and management")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/price-configs")
    public ResponseEntity<PriceConfigResponseDTO> getPriceConfigs() {
        return ResponseEntity.ok(adminService.getPriceConfigs());
    }

    @Operation(summary = "Update price configurations", description = "Admin updates price configurations with new values for different ride types and conditions")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/price-configs")
    public ResponseEntity<Void> updatePriceConfigs(@Valid @RequestBody PriceConfigRequestDTO request) {
        adminService.updatePriceConfigs(request);
        return ResponseEntity.ok().build();
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/report")
    public ResponseEntity<AdminReportResponseDTO> getReportGet(
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate,
            @RequestParam(required = false, defaultValue = "false") boolean drivers,
            @RequestParam(required = false, defaultValue = "false") boolean users,
            @RequestParam(required = false) String personId
    ) {
        return ResponseEntity.ok(adminService.getReport(startDate,endDate,drivers,users,personId));
    }
}
