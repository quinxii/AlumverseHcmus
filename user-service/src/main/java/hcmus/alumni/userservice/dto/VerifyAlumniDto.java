package hcmus.alumni.userservice.dto;

import hcmus.alumni.userservice.model.VerifyAlumniModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class VerifyAlumniDto {
	private String id;
	private String studentId;
	private Integer beginningYear;
	private String socialMediaLink;
	private String comment;
	private VerifyAlumniModel.Status status;
	private String email;
	private String fullName;
	private String avatarUrl;
	private String facultyName;
}