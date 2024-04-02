package hcmus.alumni.halloffame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HallOfFameDto {
	private String id;
	private String title;
	private String content;
	private String thumbnail;
	private Integer views;
	private String faculty;
	private Integer beginningYear;
}