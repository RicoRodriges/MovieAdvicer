package sp.advicer.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Repository
public class TmdbApi {
    private final RestTemplate restTemplate;
    private final String key;
    private final String host;

    @Autowired
    public TmdbApi(@Value("${api.key}") String apiKey,
                   @Value("${api.host}") String apiHost) {
        this.key = apiKey;
        this.host = apiHost;
        this.restTemplate = new RestTemplate();
    }

    public List<Actor> getActorsByFilmId(Integer id) {
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(host)
                .pathSegment("movie", id.toString(), "credits")
                .queryParam("api_key", key)
                .build();
        ResponseEntity<ResponseForCast> response = exec(() -> restTemplate.getForEntity(uriBuilder.toString(), ResponseForCast.class));
        return response.getBody().getCast();
    }

    public List<Keyword> getKeywordsByFilmId(Integer id) {
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(host)
                .pathSegment("movie", id.toString(), "keywords")
                .queryParam("api_key", key)
                .build();
        ResponseEntity<Keywords> response_keywords = exec(() -> restTemplate.getForEntity(uriBuilder.toString(), Keywords.class));
        return response_keywords.getBody().getKeywords();
    }

    public Film getMovieById(Integer id) {
        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(host)
                .pathSegment("movie", id.toString())
                .queryParam("api_key", key)
                .build();
        ResponseEntity<Film> response = exec(() -> restTemplate.getForEntity(uriBuilder.toString(), Film.class));
        return response.getBody();
    }

    private ResponseForResults getResponseFromDiscover(Integer page, Map<String, String> parameters) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(host)
                .pathSegment("discover", "movie")
                .queryParam("api_key", key)
                .queryParam("sort_by", "vote_count.desc")
                .queryParam("include_video", "false")
                .queryParam("include_adult", "true")
                .queryParam("page", page);
        parameters.forEach(uriComponentsBuilder::queryParam);
        return exec(() -> restTemplate.getForEntity(uriComponentsBuilder.build().toString(), ResponseForResults.class)).getBody();
    }

    public List<Film> getAllFilmsByParameters(int size, Map<String, String> parameters) {
        List<Film> films = new ArrayList<>(size);
        int page = 1;
        do {
            ResponseForResults response = getResponseFromDiscover(page, parameters);
            if (response.getTotalPages() == -1) {
                break;
            }
            response.getResults().stream()
                    .limit(size - films.size())
                    .forEach(films::add);
            if (response.getTotalPages() <= page) {
                break;
            }
            page++;
        } while (films.size() < size);
        return films;
    }

    private static <T> T exec(Supplier<T> func) {
        int count = 5;
        int timeout = 3;
        while (true) {
            try {
                return func.get();
            } catch (RestClientException e) {
                if (count >= 0 && e.getMessage().contains("429")) {
                    try {
                        TimeUnit.SECONDS.sleep(timeout);
                    } catch (InterruptedException ex) {
                    }
                    count--;
                } else {
                    throw e;
                }
            }
        }
    }
}
