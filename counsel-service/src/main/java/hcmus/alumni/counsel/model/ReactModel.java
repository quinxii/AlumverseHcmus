package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "react")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReactModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "TINYINT")
    private Integer id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private Date updateAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete = false;

    public ReactModel(Integer id) {
        this.id = id;
    }
}
