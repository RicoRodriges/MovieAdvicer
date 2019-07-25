package sp.advicer.entity.dto.film;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Film {
    private String title;
    @JsonProperty("original_title")
    private String originalTitle;
    private float popularity;
    private boolean adult;
    private Integer id;
    @JsonProperty("vote_average")
    private float voteAverage;
    @JsonProperty("poster_path")
    private String posterPath;
    private String overview;
    private List<Genre> genres;
}
