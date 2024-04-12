package hcmus.alumni.event.dto;

import java.util.Date;
import java.util.Set;

import hcmus.alumni.event.model.TagModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class EventDto {
	private String id;
	private String title;
	private String thumbnail;
	private Integer views;
	private Date publishedAt;
	private Set<TagModel> tags;
}
