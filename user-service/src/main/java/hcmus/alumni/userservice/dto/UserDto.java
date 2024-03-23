package hcmus.alumni.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserDto {
	String email;
	String fullName;
	String avatarUrl;
}
