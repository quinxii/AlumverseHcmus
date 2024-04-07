package hcmus.alumni.news.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NewsDto {
	private String id;
	private String title;
	private String content;
	private String thumbnail;
	private Integer views;
}
