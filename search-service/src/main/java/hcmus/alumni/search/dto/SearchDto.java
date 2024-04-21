package hcmus.alumni.search.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchDto {

	private String id;
	private String title;
	private String thumbnail;
	private Integer views;
	private String faculty;
	private Integer beginningYear;
	
	private Date publishedAt;

}