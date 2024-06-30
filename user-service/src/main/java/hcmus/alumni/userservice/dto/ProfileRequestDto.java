package hcmus.alumni.userservice.dto;

import lombok.Data;

@Data
public class ProfileRequestDto {
    private UserDto user;
    private AlumniDto alumni;
}
