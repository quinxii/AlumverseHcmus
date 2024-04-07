package hcmus.alumni.news.dto;

public interface INewsDto {
//	interface User{
//		
//	}
	interface StatusPost{
		String getName();
	}
	
	String getId();
	String getTitle();
	String getContent();
	String getThumbnail();
	Integer getViews();
	StatusPost getStatus();
}
