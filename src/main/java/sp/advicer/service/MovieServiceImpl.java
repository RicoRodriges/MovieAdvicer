package sp.advicer.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import sp.advicer.entity.dto.film.Film;


@Service
public class MovieServiceImpl implements MovieService {
    MovieServiceAsync msa = new MovieServiceAsync(); 
	private void deleteBaseIdInMap(List<Film> baseFilms,Map<Integer,Integer> films_with_rate){
		for (Film film:baseFilms){
			films_with_rate.remove(film.getId());
		}
	}

	private List<Integer> getListIdFromMap(Integer number_of_films, Map<Integer,Integer> films_with_rate){
		Map<Integer, Integer> films =
				films_with_rate.entrySet()
				               .stream()
				               .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				               .limit(number_of_films)
				               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,LinkedHashMap::new));
		List<Integer>films_list= new ArrayList<Integer>();
		films_list.addAll(films.keySet());
		return films_list;
	}
	@Override
	public List<Integer> getRecomendationList(Integer number_of_films,List<Film> films) {
	    Map<Integer,Integer> films_with_rate = new ConcurrentHashMap<Integer,Integer>();
	    Future<String> castFut= msa.fillMapByCast(films, films_with_rate);
	    Future<String> genresFut= msa.fillMapByGenres(films, films_with_rate);
	    Future<String> keywordsFut= msa.fillMapByKeywords(films, films_with_rate);
		while (true){
			if (castFut.isDone()&&genresFut.isDone()&&keywordsFut.isDone()){
				try {
					System.out.println(castFut.get());
					System.out.println(genresFut.get());
					System.out.println(keywordsFut.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} 
				break;					
			}
		}		
		deleteBaseIdInMap(films,films_with_rate);
		return getListIdFromMap(number_of_films,films_with_rate);
	}
}
