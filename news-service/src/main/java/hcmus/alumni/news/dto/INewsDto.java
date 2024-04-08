package hcmus.alumni.news.dto;

import java.util.Set;

public interface INewsDto {
//	interface User{
//		
//	}
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
	Set<Tag> getTags();
	Faculty getFaculty();
	StatusPost getStatus();
}
