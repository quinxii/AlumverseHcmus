package hcmus.alumni.userservice.dto.user;

public interface ISuggestionUserDto {
    interface Role {
        Integer getId();
        String getName();
    }

    String getId();
    String getFullName();
    String getEmail();
    String getAvatarUrl();
    Integer getStatusId();
}
