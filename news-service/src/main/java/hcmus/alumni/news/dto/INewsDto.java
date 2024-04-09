package hcmus.alumni.news.dto;

import java.util.Date;
import java.util.Set;

public interface INewsDto {
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
}
