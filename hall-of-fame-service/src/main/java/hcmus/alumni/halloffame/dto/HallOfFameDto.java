package hcmus.alumni.halloffame.dto;

import java.util.Date;
import java.util.Set;

import hcmus.alumni.halloffame.model.TagModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HallOfFameDto {

	private String id;
	private String title;
	private String thumbnail;
	private Integer views;
	private String faculty;
	private Integer beginningYear;
	
	private Date publishedAt;
	
	private Set<TagModel> tags;

}