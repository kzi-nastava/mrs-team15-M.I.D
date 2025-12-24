package rs.ac.uns.ftn.asd.ridenow.service;


@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponseDTO getProfile() {
        User user = getLoggedUser();

        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.email = user.getEmail();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.adress = user.getAddress();
        dto.phoneNumber = user.getPhoneNumber();
        dto.profileImage = user.getProfileImage();

        return dto;
    }

    public void updateProfile(UpdateProfileRequestDTO dto) {
        User user = getLoggedUser();

        user.setFirstName(dto.firstName);
        user.setLastName(dto.lastName);

        if (dto.profileImage != null) {
            user.setProfileImage(dto.profileImage);
        }

        userRepository.save(user);
    }

    public void changePassword(ChangePasswordRequestDTO dto) {
        User user = getLoggedUser();

        if (!passwordEncoder.matches(dto.oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        if (passwordEncoder.matches(dto.newPassword, dto.confirmNewPassword)) {
            throw new RuntimeException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword));
        userRepository.save(user);
    }

    private User getLoggedUser() {
        return null;
    }
}
