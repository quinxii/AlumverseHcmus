package hcmus.alumni.userservice.dto.user;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResetPasswordRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    String email;
    String oldPassword;
    String newPassword;
}
