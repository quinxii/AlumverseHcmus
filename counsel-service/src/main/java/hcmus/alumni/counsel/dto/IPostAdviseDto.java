package hcmus.alumni.counsel.dto;

import java.util.Date;
import java.util.Set;

public interface IPostAdviseDto {
	interface User{
		String getFullName();
		String getAvatarUrl();
	}
	interface StatusPost{
		String getName();
	}
	interface Tag{
		Integer getId();
		String getName();
	}
	
	String getId();
	String getTitle();
	String getContent();
	Integer getChildrenCommentNumber();
	Date getUpdateAt();
	Date getPublishedAt();
	User getCreator();
	Set<Tag> getTags();
	StatusPost getStatus();
}