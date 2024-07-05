package hcmus.alumni.userservice.dto;

import java.util.Date;

import lombok.Data;

@Data
public class UserDto {
	private String userId;
    private String fullName;
    private Date dob;
    private Integer facultyId;
    private Integer sexId;
    private String socialMediaLink;
    private String phone;
    private String aboutMe;
}
