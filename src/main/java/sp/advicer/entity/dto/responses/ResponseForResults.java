package sp.advicer.entity.dto.responses;

import java.util.List;

import lombok.Data;
import sp.advicer.entity.dto.film.Film;

import com.fasterxml.jackson.annotation.JsonProperty;
@Data
public class ResponseForResults {
	Integer page;
	@JsonProperty("total_pages")
	Integer totalPages;
	List<Film> results;
}
