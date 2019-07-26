package sp.advicer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.repository.TmdbApi;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class MovieServiceImpl {
    private final MovieServiceAsync msa;
    private final TmdbApi api;

    private List<Film> getListFilmsByIds(Collection<Integer> ids) {
        return ids.stream()
                .distinct()
                .map(api::getMovieById)
                .collect(Collectors.toList());
    }

    public List<Film> getRecomendationList(int filmCount, Collection<Integer> ids) {
        List<Film> films = getListFilmsByIds(ids);
        Future<Map<Film, Integer>> castFut = msa.getFilmWithScoreByCast(films);
        Future<Map<Film, Integer>> genresFut = msa.getFilmWithScoreByGenres(films);
        Future<Map<Film, Integer>> keywordsFut = msa.getFilmWithScoreByKeywords(films);

        Map<Film, Integer> filmsWithRate;
        try {
            filmsWithRate = mergeFilmMapWithScore(Arrays.asList(
                    castFut.get(),
                    genresFut.get(),
                    keywordsFut.get()
            ));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return filmsWithRate.entrySet().stream()
                .filter(entry -> !films.contains(entry.getKey()))
                .sorted(Comparator.<Map.Entry<Film, Integer>>comparingInt(Map.Entry::getValue).reversed())
                .map(Map.Entry::getKey)
                .limit(filmCount)
                .collect(Collectors.toList());
    }

    private Map<Film, Integer> mergeFilmMapWithScore(Collection<Map<Film, Integer>> scoreMaps) {
        return scoreMaps.stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
    }
}
