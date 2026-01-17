package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.*;
import rs.ac.uns.ftn.asd.ridenow.model.ActivationToken;
import rs.ac.uns.ftn.asd.ridenow.model.ForgotPasswordToken;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.ActivationTokenRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.ForgotPasswordTokenRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.security.JwtUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private  EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    String profileImageURL = "/uploads/default.png";

    public RegisterResponseDTO register(RegisterRequestDTO requestDTO, MultipartFile profileImage) throws Exception{
        if(userRepository.findByEmail(requestDTO.getEmail()).isPresent()){
            throw new Exception("User with this email already exists");
        }

        String profileImageURL = generateProfileImageUrl(profileImage);
        String hashedPassword = passwordEncoder.encode(requestDTO.getPassword());

        RegisteredUser user = new RegisteredUser(requestDTO.getEmail(), hashedPassword, requestDTO.getFirstName(),
                requestDTO.getLastName(), requestDTO.getPhoneNumber(), requestDTO.getAddress(),
                profileImageURL, false, false);
        User savedUser = userRepository.save(user);

        sendActivationEmail(savedUser);

        RegisterResponseDTO responseDTO = new RegisterResponseDTO();
        responseDTO.setId(savedUser.getId());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setFirstName(savedUser.getFirstName());
        responseDTO.setLastName(savedUser.getLastName());
        responseDTO.setActive(savedUser.isActive());
        return  responseDTO;
    }

    private void sendActivationEmail(User user) {
        ActivationToken oldToken = user.getActivationToken();
        if (oldToken != null) {
            user.setActivationToken(null);
            activationTokenRepository.delete(oldToken);
            userRepository.save(user);
        }
        ActivationToken token = generateActivationToken(user);
        activationTokenRepository.save(token);
        emailService.sendActivationMail(user.getEmail(), token);
    }

    private ActivationToken generateActivationToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        ActivationToken activationToken = new ActivationToken(token, expiresAt, user);
        user.setActivationToken(activationToken);
        return activationToken;
    }

    private String generateProfileImageUrl(MultipartFile profileImage) throws IOException {
        String imageURL = profileImageURL;
        if (profileImage != null && !profileImage.isEmpty()) {
            String uploadDir = "uploads/";
            Files.createDirectories(Paths.get(uploadDir));

            String fileName = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            imageURL = "/uploads/" + fileName;
        }
        return imageURL;
    }

    public void handleExpiredActivationToken(ActivationToken activationToken) {
        User user = activationToken.getUser();
        user.setActivationToken(null);
        activationTokenRepository.delete(activationToken);
        sendActivationEmail(user);
    }

    public LoginResponseDTO login(LoginRequestDTO requestDTO) throws Exception {
        Optional<User> user = userRepository.findByEmail(requestDTO.getEmail());
        if(user.isEmpty()) {
            throw new Exception("User with this email does not exists");
        }
        User existingUser = user.get();
        if(!existingUser.isActive()){
            throw  new Exception("Account is not active. Please activate your account via email.");
        }
        if(!passwordEncoder.matches(requestDTO.getPassword(), existingUser.getPassword())) {
            throw new Exception("Invalid credentials");
        }
        String token = jwtUtil.generateJWTToken(requestDTO.getEmail());
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setToken(token);
        responseDTO.setRole(existingUser.getRole().name());
        return responseDTO;
    }

    public void forgotPassword(ForgotPasswordRequestDTO request) throws  Exception{
        Optional<User> user = userRepository.findByEmail(request.getEmail());
        if(user.isEmpty()){
            throw  new Exception("User with this email does not exists");
        }
        User existingUser = user.get();
        if(!existingUser.isActive()){
            throw  new Exception("Account is not active. Please activate your account via email first.");
        }
        sendForgotPasswordEmail(existingUser);
    }

    private ForgotPasswordToken generateForgotPasswordToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(token, expiresAt, user);
        user.setForgotPasswordToken(forgotPasswordToken);
        return forgotPasswordToken;
    }

    private void sendForgotPasswordEmail(User user) {
        ForgotPasswordToken oldToken = user.getForgotPasswordToken();
        if (oldToken != null) {
            user.setForgotPasswordToken(null);
            forgotPasswordTokenRepository.delete(oldToken);
            userRepository.save(user);
        }
        ForgotPasswordToken token = generateForgotPasswordToken(user);
        forgotPasswordTokenRepository.save(token);
        emailService.sendForgotPasswordMail(user.getEmail(), token);
    }

    public void handleExpiredForgotPasswordToken(ForgotPasswordToken forgotPasswordToken) {
        User user = forgotPasswordToken.getUser();
        user.setForgotPasswordToken(null);
        forgotPasswordTokenRepository.delete(forgotPasswordToken);
        sendForgotPasswordEmail(user);
    }

    public void resetPassword(User user, ResetPasswordRequestDTO request){
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
}