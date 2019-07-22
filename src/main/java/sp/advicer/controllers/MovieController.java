package sp.advicer.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;

import sp.advicer.entity.dto.film.Film;
import sp.advicer.entity.dto.film.FilmId;
import sp.advicer.repository.TmdbApi;
import sp.advicer.service.MovieService;
import sp.advicer.service.MovieServiceImpl;

@Controller
public class MovieController {
	MovieService movieService = new MovieServiceImpl();
	TmdbApi api = new TmdbApi();
	@PostMapping(path = "/moviesAdvicer")
	public ResponseEntity<String> postMovies(@RequestBody ArrayList<FilmId> ids) {
		movieService.prepare();
		for (FilmId id : ids) {
			try {
				movieService.addBaseMovie(api.getMovieById(id.getId()));
			} catch (RestClientException e) {
				if (e.getMessage().contains("429")) {
					try {
						TimeUnit.SECONDS.sleep(10);
						movieService.addBaseMovie(api.getMovieById(id.getId()));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return new ResponseEntity<>("Films were added to base", HttpStatus.OK);
	}

	@GetMapping(path = "/moviesAdvicer/get/{number}")
	public ModelAndView getRecommendedMovies(@PathVariable("number") Integer number) {
		List<Film> films = new ArrayList<Film>();
		if (number < 1) throw new RestClientException("Error number");
		List<Integer> ids = movieService.getRecomendationList(number);
		for (Integer id : ids) {
			try {
				films.add(api.getMovieById(id));
			} catch (RestClientException e) {
				if (e.getMessage().contains("429")) {
					try {
						TimeUnit.SECONDS.sleep(10);
						films.add(api.getMovieById(id));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		ModelAndView modelAndView = new ModelAndView("movies");
		modelAndView.addObject("films",films);
		return modelAndView;
	}	

	@GetMapping(path = "/moviesAdvicer/main")
	public String mainPage() {
		return "mainPage";
	}
}
