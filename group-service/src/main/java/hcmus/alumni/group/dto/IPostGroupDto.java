package hcmus.alumni.group.dto;

import java.util.Date;
import java.util.Set;

public interface IPostGroupDto {
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
	
	String getId();
	User getCreator();
	String getTitle();
	String getContent();
	String getGroupId();
	Set<Tag> getTags();
	Date getUpdateAt();
	Date getPublishedAt();
	StatusPost getStatus();
	Integer getChildrenCommentNumber();
}
