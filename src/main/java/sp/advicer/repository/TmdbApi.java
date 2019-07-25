package sp.advicer.repository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import sp.advicer.entity.dto.actor.Actor;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.entity.dto.keyword.Keyword;
import sp.advicer.entity.dto.keyword.Keywords;
import sp.advicer.entity.dto.responses.ResponseForCast;
import sp.advicer.entity.dto.responses.ResponseForResults;

import java.util.List;

@Repository
public class TmdbApi {
    RestTemplate restTemplate = new RestTemplate();
    private final String API_KEY = "3706398aaf547b46e616831a46402288";
    private final String MOVIE_URL = "https://api.themoviedb.org/3/movie/";
    private final String DISCOVER_URL = "https://api.themoviedb.org/3/discover/movie";

    public List<Actor> getActorsListById(Integer id) throws RestClientException {
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(MOVIE_URL + id + "/credits")
                .queryParam("api_key", API_KEY)
                .build();
        ResponseEntity<ResponseForCast> response = restTemplate.getForEntity(uriBuilder.toString(), ResponseForCast.class);
        return response.getBody().getCast();
    }

    public List<Keyword> getListKeywordsById(Integer id) throws RestClientException {
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(MOVIE_URL + id + "/keywords").queryParam("api_key", API_KEY)
                .build();
        ResponseEntity<Keywords> response_keywords = restTemplate.getForEntity(uriBuilder.toString(), Keywords.class);
        return response_keywords.getBody().getKeywords();
    }

    public Film getMovieById(Integer id) throws RestClientException {
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(MOVIE_URL + id).queryParam("api_key", API_KEY)
                .build();
        ResponseEntity<Film> response = restTemplate.getForEntity(uriBuilder.toString(), Film.class);
        return response.getBody();
    }

    public ResponseEntity<ResponseForResults> getResponseFromDiscover(Integer page, String parameters) {
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(DISCOVER_URL).queryParam("api_key", API_KEY)
                .queryParam("sort_by", "vote_count.desc")
                .queryParam("include_video", "false")
                .queryParam("include_adult", "true")
                .queryParam("page", page)
                .queryParam(parameters)
                .build();
        return restTemplate.getForEntity(uriBuilder.toString(), ResponseForResults.class);
    }
}
