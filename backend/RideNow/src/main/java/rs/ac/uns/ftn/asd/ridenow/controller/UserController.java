package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/rate-driver/{id}")
    public ResponseEntity<Void> rateDriver(@PathVariable int id, @Valid @RequestBody RateRequestDTO rateRequestDTO) {
        if (id <= 0) {
           return ResponseEntity.badRequest().build();
        }
        return  ResponseEntity.ok().build();
    }

    @PostMapping("/rate-vehicle/{id}")
    public ResponseEntity<Void> rateVehicle(@PathVariable int id, @Valid @RequestBody RateRequestDTO rateRequestDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return  ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDTO dto) {

        userService.changePassword(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequestDTO dto) {

        userService.updateUser(id, dto);
        return ResponseEntity.ok().build();
    }


}
