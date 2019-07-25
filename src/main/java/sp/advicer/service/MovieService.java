package sp.advicer.service;

import sp.advicer.entity.dto.film.Film;

import java.util.List;

public interface MovieService {
    public List<Integer> getRecomendationList(Integer number_of_films, List<Film> films);
}
