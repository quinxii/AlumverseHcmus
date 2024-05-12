package hcmus.alumni.counsel.dto;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface IPostAdviseDto {
	interface User{
		String getId();
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
	interface Pictures {
		String getId();
		String getPictureUrl();
	}
	interface Permissions {
		Boolean getEdit();
		Boolean getDelete();
	}
	
	String getId();
	String getTitle();
	List<Pictures> getPictures();
	String getContent();
	Integer getChildrenCommentNumber();
	Integer getReactionCount();
	Date getUpdateAt();
	Date getPublishedAt();
	User getCreator();
	Set<Tag> getTags();
	StatusPost getStatus();
	Boolean getIsReacted();
	Permissions getPermissions();
}