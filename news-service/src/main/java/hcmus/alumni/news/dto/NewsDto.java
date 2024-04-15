package hcmus.alumni.news.dto;

import java.util.Date;
import java.util.Set;

import hcmus.alumni.news.model.TagModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NewsDto {
	private String id;
	private String title;
	private String thumbnail;
	private Integer views;
	private Date publishedAt;
//	private Set<TagDto> tags;
	private Set<TagModel> tags;
	
	public NewsDto(String id, String title, String thumbnail, Integer views, Date publishedAt) {
		this.id = id;
		this.title = title;
		this.thumbnail = thumbnail;
		this.views = views;
		this.publishedAt = publishedAt;
	}
}