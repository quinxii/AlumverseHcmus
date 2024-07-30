package hcmus.alumni.userservice.dto.profile;

import hcmus.alumni.userservice.dto.role.UserDto;
import hcmus.alumni.userservice.dto.verifyAlumni.AlumniDto;
import lombok.Data;

@Data
public class ProfileRequestDto {
    private UserDto user;
    private AlumniDto alumni;
}
