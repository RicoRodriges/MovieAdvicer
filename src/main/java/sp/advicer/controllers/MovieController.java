package sp.advicer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.repository.TmdbApi;
import sp.advicer.service.MovieServiceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Controller
public class MovieController {
    private MovieServiceImpl movieService = new MovieServiceImpl();
    private TmdbApi api = new TmdbApi();

    @GetMapping(path = "/moviesAdvicer/get/{number}")
    public ModelAndView getSome(@PathVariable("number") Integer number, @RequestParam Set<Integer> ids) {
        if (number < 1) throw new IllegalArgumentException("Wrong number of films");
        if (ids.isEmpty()) throw new IllegalArgumentException("There must be at least one id.");
        List<Film> films = getListFilmsByIds(ids);
        List<Film> recommendFilms = getListFilmsByIds(movieService.getRecomendationList(number, films));
        ModelAndView modelAndView = new ModelAndView("movies");
        modelAndView.addObject("films", recommendFilms);
        return modelAndView;
    }

    private List<Film> getListFilmsByIds(Collection<Integer> ids) {
        List<Film> films = new ArrayList<Film>();
        for (Integer id : ids) {
            films.add(api.getMovieById(id));
        }
        return films;
    }

    @GetMapping(path = "/moviesAdvicer/main")
    public String mainPage() {
        return "mainPage";
    }
}
