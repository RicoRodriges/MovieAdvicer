package sp.advicer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import sp.advicer.entity.dto.actor.Actor;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.entity.dto.film.Genre;
import sp.advicer.entity.dto.responses.ResponseForResults;
import sp.advicer.repository.TmdbApi;

import java.util.*;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class MovieServiceAsync {
    private static final int PAGE_GENRES = 3;
    private static final int PAGE_ACTOR = 2;
    private static final int MAX_FILM_COUNT_BY_KEYWORD = 60;

    private final TmdbApi api;

    private void addBaseMovieCast(Actor actor, Map<Integer, Integer> cast_with_count) {
        if (cast_with_count.containsKey(actor.getId())) {
            int count = cast_with_count.get(actor.getId());
            cast_with_count.put(actor.getId(), count + 1);
        } else {
            cast_with_count.put(actor.getId(), 1);
        }
    }

    private void addIdstoMap(List<Film> films, Map<Integer, Integer> films_with_rate) {
        for (Film film : films) {
            if (films_with_rate.containsKey(film.getId())) {
                int rate = films_with_rate.get(film.getId()).intValue();
                films_with_rate.put(film.getId(), rate + 1);
            } else {
                films_with_rate.put(film.getId(), 1);
            }
        }
    }

    private void addFilmsToMapWithScore(List<Film> films, Map<Film, Integer> filmScores) {
        for (Film film : films) {
            filmScores.compute(film, (f, s) -> (s != null ? (s + 1) : 1));
        }
    }

    private void fillActorsByFilm(Film film, Map<Integer, Integer> cast_with_count) {
        List<Actor> actors = api.getActorsListById(film.getId());
        for (Actor actor : actors) {
            addBaseMovieCast(actor, cast_with_count);
        }
    }

    @Async
    public Future<String> fillMapByCast(List<Film> baseFilms, Map<Integer, Integer> films_with_rate) {
        Map<Integer, Integer> cast_with_count = new HashMap<Integer, Integer>();
        for (Film film : baseFilms) {
            fillActorsByFilm(film, cast_with_count);
        }
        if (cast_with_count.isEmpty()) return new AsyncResult<String>("Maps update by zero actors.");
        cast_with_count.forEach((id_actor, value) -> {
            int total_pages = -1;
            for (int page = 1; page < PAGE_ACTOR; page++) {
                if (page > total_pages && total_pages != -1) break;
                ResponseForResults response = api.getResponseFromDiscover(page, Collections.singletonMap("with_people", id_actor.toString()));
                if (total_pages == -1) total_pages = response.getTotalPages();
                addIdstoMap(response.getResults(), films_with_rate);
            }
        });
        return new AsyncResult<String>("Maps update by actors.");
    }

    @Async
    public Future<String> fillMapByGenres(List<Film> baseFilms, Map<Integer, Integer> films_with_rate) {
        for (Film film : baseFilms) {
            List<Genre> genres = film.getGenres();
            for (Genre genre : genres) {
                int total_pages = -1;
                for (int page = 1; page < PAGE_GENRES; page++) {
                    if (page > total_pages && total_pages != -1) break;
                    ResponseForResults response = api.getResponseFromDiscover(page, Collections.singletonMap("with_genres", genre.getId().toString()));
                    if (total_pages == -1) total_pages = response.getTotalPages();
                    addIdstoMap(response.getResults(), films_with_rate);
                }
            }
        }
        return new AsyncResult<String>("Maps update by genres.");
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
