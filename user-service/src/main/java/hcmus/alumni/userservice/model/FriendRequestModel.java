package hcmus.alumni.userservice.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "request_friend")
public class FriendRequestModel {

    @EmbeddedId
    private FriendRequestId id;
    
    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserModel user;
	
	@MapsId("friendId")
    @ManyToOne
    @JoinColumn(name = "friend_id", referencedColumnName = "id", nullable = false)
    private UserModel friend;

    @CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;
    
}
