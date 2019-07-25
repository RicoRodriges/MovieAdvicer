package sp.advicer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.repository.TmdbApi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MovieServiceImpl {
    private final MovieServiceAsync msa;
    private final TmdbApi api;

    private void deleteBaseIdInMap(List<Film> baseFilms, Map<Integer, Integer> films_with_rate) {
        for (Film film : baseFilms) {
            films_with_rate.remove(film.getId());
        }
    }

    private List<Integer> getListIdFromMap(Integer number_of_films, Map<Integer, Integer> films_with_rate) {
        Map<Integer, Integer> films =
                films_with_rate.entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                        .limit(number_of_films)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        List<Integer> films_list = new ArrayList<Integer>();
        films_list.addAll(films.keySet());
        return films_list;
    }

    private List<Film> getListFilmsByIds(Collection<Integer> ids) {
        return ids.stream()
                .distinct()
                .map(api::getMovieById)
                .collect(Collectors.toList());
    }

    public List<Film> getRecomendationList(Integer number_of_films, Collection<Integer> ids) {
        List<Film> films = getListFilmsByIds(ids);
        Map<Integer, Integer> films_with_rate = new ConcurrentHashMap<Integer, Integer>();
        Future<String> castFut = msa.fillMapByCast(films, films_with_rate);
        Future<String> genresFut = msa.fillMapByGenres(films, films_with_rate);
        Future<String> keywordsFut = msa.fillMapByKeywords(films, films_with_rate);

        try {
            System.out.println(castFut.get());
            System.out.println(genresFut.get());
            System.out.println(keywordsFut.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        deleteBaseIdInMap(films, films_with_rate);
        return getListFilmsByIds(getListIdFromMap(number_of_films, films_with_rate));
    }
}
