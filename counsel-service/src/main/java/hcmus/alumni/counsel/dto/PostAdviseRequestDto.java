package hcmus.alumni.counsel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostAdviseRequestDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TagRequestDto {
        private Integer id;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteRequestDto {
        private Integer id;
        private String name;
    }

    String title;
    String content;
    List<TagRequestDto> tags;
    List<VoteRequestDto> votes;
}