package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.PanicAlertDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.PanicAlertService;

import java.util.List;

@RestController
@RequestMapping("/api/panic-alerts")
public class PanicAlertController {
    @Autowired
    private PanicAlertService panicAlertService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unresolved")
    public ResponseEntity<List<PanicAlertDTO>> getUnresolvedAlerts(){
        return ResponseEntity.ok(panicAlertService.getAllUnresolvedAlerts());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAlertById(@PathVariable Long id){
        try{
            return ResponseEntity.ok(panicAlertService.getAlertById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<PanicAlertDTO>> getAllAlerts(@RequestParam(defaultValue = "false") boolean includeResolved){
        if(includeResolved){
            return ResponseEntity.ok(panicAlertService.getAllAlerts());
        }
        return  ResponseEntity.ok(panicAlertService.getAllUnresolvedAlerts());
    }

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