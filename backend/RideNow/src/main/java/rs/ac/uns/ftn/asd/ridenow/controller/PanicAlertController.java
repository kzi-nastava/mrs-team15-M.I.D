package rs.ac.uns.ftn.asd.ridenow.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.PanicAlertDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.PanicAlertService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Panic Alerts", description = "Panic alert management endpoints")
@RestController
@RequestMapping("/api/panic-alerts")
public class PanicAlertController {
    @Autowired
    private PanicAlertService panicAlertService;

    @Operation(summary = "Get unresolved panic alerts", description = "Admin retrieves a paginated list of all unresolved panic alerts sorted by creation time")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unresolved")
    public ResponseEntity<Page<PanicAlertDTO>> getUnresolvedAlerts(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(panicAlertService.getUnresolvedAlerts(pageable));
    }

    @Operation(summary = "Get panic alert by ID", description = "Admin retrieves details of a specific panic alert by its ID")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAlertById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(panicAlertService.getAlertById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get all panic alerts", description = "Admin retrieves a list of all panic alerts with an option to include resolved ones")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<PanicAlertDTO>> getAllAlerts(@RequestParam(defaultValue = "false") boolean includeResolved){
        if(includeResolved){
            return ResponseEntity.ok(panicAlertService.getAllAlerts());
        }
        return  ResponseEntity.ok(panicAlertService.getAllUnresolvedAlerts());
    }

    @Operation(summary = "Resolve panic alert", description = "Admin marks a panic alert as resolved by its ID")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolvePanicAlert(@PathVariable Long id){
        try{
            User admin = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            panicAlertService.resolvePanicAlert(id, admin.getId());
            return ResponseEntity.ok().body("Panic alert resolved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}