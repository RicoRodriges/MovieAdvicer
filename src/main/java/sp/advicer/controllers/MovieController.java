package sp.advicer.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.service.MovieServiceImpl;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/moviesAdvicer")
@RequiredArgsConstructor
public class MovieController {
    private final MovieServiceImpl movieService;

    @GetMapping(path = "/propose")
    public ModelAndView proposePage(@RequestParam("count") int number,
                                    @RequestParam("ids") Set<Integer> ids) {
        if (number < 1) {
            throw new IllegalArgumentException("Wrong number of films");
        }
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one id.");
        }
        List<Film> recommendFilms = movieService.getRecomendationList(number, ids);
        return new ModelAndView("movies").addObject("films", recommendFilms);
    }

    @GetMapping(path = "/main")
    public String mainPage() {
        return "mainPage";
    }

    @GetMapping(path = "/movies")
    @ResponseBody
    public List<Film> findFilmByName(@RequestParam("q") String name) {
        return movieService.findFilmsByName(name);
    }
}
