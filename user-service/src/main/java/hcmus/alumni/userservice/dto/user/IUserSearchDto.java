package hcmus.alumni.userservice.dto.user;

import java.util.Set;

public interface IUserSearchDto {
    interface Role {
        Integer getId();
        String getName();
    }

    String getId();
    String getFullName();
    String getEmail();
    String getAvatarUrl();
    Set<Role> getRoles();
    Integer getStatusId();
}
