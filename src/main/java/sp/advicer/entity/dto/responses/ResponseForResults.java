package sp.advicer.entity.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import sp.advicer.entity.dto.film.Film;

import java.util.List;

@Data
public class ResponseForResults {
    Integer page;
    @JsonProperty("total_pages")
    Integer totalPages;
    List<Film> results;
}
