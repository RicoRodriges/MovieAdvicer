package sp.advicer.service;

import java.util.List;

import sp.advicer.entity.dto.film.Film;

public interface MovieService {
	public List<Integer> getRecomendationList(Integer number_of_films,List<Film>films);
}
