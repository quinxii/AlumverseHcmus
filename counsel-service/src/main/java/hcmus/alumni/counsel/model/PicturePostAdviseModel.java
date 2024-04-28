package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "picture_post_advise")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PicturePostAdviseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Id
    @ManyToOne
    @JoinColumn(name = "post_advise_id", nullable = false)
    private PostAdviseModel postAdvise;

    @Column(name = "picture_url", nullable = false, length = 255)
    private String pictureUrl;

    @Column(name = "picture_order", nullable = false, columnDefinition = "TINYINT")
    private Integer pitctureOrder;
}
