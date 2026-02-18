package rs.ac.uns.ftn.asd.ridenow.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for registering/updating user's FCM device token
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenDTO {
    private String token;
}
