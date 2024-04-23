package hcmus.alumni.group.dto;

public interface IGroupDto {
    interface User {
        String getFullName();
        String getAvatarUrl();
    }
    
    interface Status {
        String getFullName();
        String getAvatarUrl();
    }

    String getId();
    String getName();
    User getCreator();
    String getType();
    String getAvatarUrl();
    String getCoverUrl();
    String getWebsite();
    Status getStatus();

    enum Privacy {
        PUBLIC, PRIVATE
    }
}

