package hcmus.alumni.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchDto {

	private String id;
	private String fullName;
	private String avatarUrl;
	private String faculty;
	private Integer beginningYear;
	private String socialMediaLink;
	
}