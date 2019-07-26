package sp.advicer.entity.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import sp.advicer.entity.dto.film.Film;

import java.util.List;

@Data
public class ResponseForResults {
    private Integer page;
    @JsonProperty("total_pages")
    private Integer totalPages;
    private List<Film> results;
}
