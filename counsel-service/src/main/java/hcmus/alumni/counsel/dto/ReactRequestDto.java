package hcmus.alumni.counsel.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReactRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    Integer reactId;
}
