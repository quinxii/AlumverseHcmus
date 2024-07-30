package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "[tag]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TagModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, columnDefinition = "INT UNSIGNED")
	private Long id;

	@Column(name = "name", length = 100, nullable = false, unique = true)
	private String name;

	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;

	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;

	public TagModel(Long id) {
		this.id = id;
	}

	public TagModel(String name) {
		this.name = name;
	}

	public static String sanitizeTagName(String name) {
		return name.trim().replaceAll(" +", " ").toLowerCase();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		TagModel tagModel = (TagModel) o;
		return name.equals(tagModel.name);
	}

	@Override
	public int hashCode() {
		return name == null ? 0 : name.hashCode();
	}
}
