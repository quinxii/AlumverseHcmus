package hcmus.alumni.userservice.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FriendId implements Serializable {

    @Column(name = "user_id", length=36, nullable = false)
    private String userId;

    @Column(name = "friend_id", length=36, nullable = false)
    private String friendId;

}
