package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.RegisterRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.RegisterResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    String profileImageURL = "/uploads/default.png";

    public RegisterResponseDTO register(RegisterRequestDTO requestDTO, MultipartFile profileImage) throws Exception{
        if(userRepository.findByEmail(requestDTO.getEmail()).isPresent()){
            throw new Exception("User with this email already exists");
        }
        String profileImageURL = generateProfileImageUrl(profileImage);
        // add hashed value maybe here
        User user = new User(requestDTO.getEmail(), requestDTO.getPassword(), requestDTO.getFirstName(),
                requestDTO.getLastName(), requestDTO.getPhoneNumber(), requestDTO.getAddress(),
                profileImageURL, false, false);
        User savedUser = userRepository.save(user);
        RegisterResponseDTO responseDTO = new RegisterResponseDTO();
        responseDTO.setId(savedUser.getId());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setFirstName(savedUser.getFirstName());
        responseDTO.setLastName(savedUser.getLastName());
        responseDTO.setActive(savedUser.isActive());
        return  responseDTO;
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
}