package hcmus.alumni.event.dto;

import java.util.Date;
import java.util.Set;

import hcmus.alumni.event.model.TagModel;

public interface IEventDto {
	interface User{
		String getFullName();
	}
	interface StatusPost{
		String getName();
	}
	interface Tag{
		Integer getId();
		String getName();
	}
	interface Faculty{
		Integer getId();
		String getName();
	}

	String getId();
	String getTitle();
	String getContent();
	String getThumbnail();
	Integer getViews();
	Date getUpdateAt();
	Date getPublishedAt();
	User getCreator();
	Set<Tag> getTags();
	Faculty getFaculty();
	StatusPost getStatus();
	Long getParticipants();
}
