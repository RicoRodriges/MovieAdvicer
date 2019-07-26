package sp.advicer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.repository.TmdbApi;

import java.util.*;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class MovieServiceAsync {
    private static final int MAX_FILM_COUNT_BY_GENRES = 60;
    private static final int MAX_FILM_COUNT_BY_ACTOR = 40;
    private static final int MAX_FILM_COUNT_BY_KEYWORD = 60;

    private final TmdbApi api;

    private void addFilmsToMapWithScore(List<Film> films, Map<Film, Integer> filmScores) {
        for (Film film : films) {
            filmScores.compute(film, (f, s) -> (s != null ? (s + 1) : 1));
        }
    }

    @Async
    public Future<Map<Film, Integer>> getFilmWithScoreByCast(List<Film> baseFilms) {
        HashMap<Film, Integer> result = new HashMap<>();

        baseFilms.stream()
                .map(Film::getId)
                .map(api::getActorsByFilmId)
                .flatMap(Collection::stream)
                .forEach(actor -> {
                    List<Film> films = api.getAllFilmsByParameters(MAX_FILM_COUNT_BY_ACTOR, Collections.singletonMap("with_people", actor.getId().toString()));
                    addFilmsToMapWithScore(films, result);
                });

        return new AsyncResult<>(result);
    }

    @Async
    public Future<Map<Film, Integer>> getFilmWithScoreByGenres(List<Film> baseFilms) {
        HashMap<Film, Integer> result = new HashMap<>();

        baseFilms.stream()
                .map(Film::getGenres)
                .flatMap(Collection::stream)
                .forEach(genre -> {
                    List<Film> films = api.getAllFilmsByParameters(MAX_FILM_COUNT_BY_GENRES, Collections.singletonMap("with_genres", genre.getId().toString()));
                    addFilmsToMapWithScore(films, result);
                });

        return new AsyncResult<>(result);
    }

    @Async
    public Future<Map<Film, Integer>> getFilmWithScoreByKeywords(List<Film> baseFilms) {
        HashMap<Film, Integer> result = new HashMap<>();

        baseFilms.stream()
                .map(Film::getId)
                .map(api::getKeywordsByFilmId)
                .flatMap(Collection::stream)
                .forEach(keyword -> {
                    List<Film> films = api.getAllFilmsByParameters(MAX_FILM_COUNT_BY_KEYWORD, Collections.singletonMap("with_keywords", keyword.getId().toString()));
                    addFilmsToMapWithScore(films, result);
                });

        return new AsyncResult<>(result);
    }
}
