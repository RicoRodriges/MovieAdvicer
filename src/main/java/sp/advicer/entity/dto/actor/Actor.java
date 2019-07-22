package sp.advicer.entity.dto.actor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.entity.dto.film.Genre;

import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Actor {
    private Integer id;
    private String name;
}
