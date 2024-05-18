package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendId implements Serializable {
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserModel user;
	
	@ManyToOne
	@JoinColumn(name = "friend_id")
	private UserModel friend;
}
