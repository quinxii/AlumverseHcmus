package hcmus.alumni.group.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostGroupRequestDto {
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
    Boolean allowMultipleVotes = false;
    Boolean allowAddOptions = false;
    List<TagRequestDto> tags;
    List<VoteRequestDto> votes;
}
