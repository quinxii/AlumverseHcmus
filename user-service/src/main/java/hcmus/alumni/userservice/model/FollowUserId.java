package hcmus.alumni.userservice.model;

import java.io.Serializable;

import javax.persistence.Column;

public class FollowUserId implements Serializable {
	
	@Column(name = "user_id", length=36, nullable = false)
    private String userId;

    @Column(name = "follower_id", length=36, nullable = false)
    private String followerId;
    
}
