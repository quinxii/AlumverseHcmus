package hcmus.alumni.counsel.dto.request;

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
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoteRequestDto {
        private String name;
    }

    String title;
    String content;
    List<TagRequestDto> tags;
    List<VoteRequestDto> votes;
}