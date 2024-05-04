package hcmus.alumni.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchDto {

	private String id;
	private String fullName;
	private String thumbnail;
	private String faculty;
	private Integer beginningYear;

	
}