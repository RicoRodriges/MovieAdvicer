package sp.advicer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.service.MovieServiceImpl;

import java.util.List;
import java.util.Set;

@Controller
public class MovieController {
    private MovieServiceImpl movieService = new MovieServiceImpl();

    @GetMapping(path = "/moviesAdvicer/get/{number}")
    public ModelAndView getSome(@PathVariable("number") Integer number, @RequestParam Set<Integer> ids) {
        if (number < 1) throw new IllegalArgumentException("Wrong number of films");
        if (ids.isEmpty()) throw new IllegalArgumentException("There must be at least one id.");
        List<Film> recommendFilms = movieService.getRecomendationList(number, ids);
        ModelAndView modelAndView = new ModelAndView("movies");
        modelAndView.addObject("films", recommendFilms);
        return modelAndView;
    }

    @GetMapping(path = "/moviesAdvicer/main")
    public String mainPage() {
        return "mainPage";
    }
}
