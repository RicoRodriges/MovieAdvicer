package sp.advicer.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import sp.advicer.entity.dto.actor.Actor;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.entity.dto.film.Genre;
import sp.advicer.entity.dto.keyword.Keyword;
import sp.advicer.entity.dto.responses.ResponseForResults;
import sp.advicer.repository.TmdbApi;

@Component
public class MovieServiceAsync {
	private final int PAGE_GENRES=3;
	private final int PAGE_ACTOR=2;
	private final int PAGE_KEYWORDS=3;
	private TmdbApi api =new TmdbApi();
	private void addBaseMovieCast(Actor actor,Map<Integer,Integer> cast_with_count) {
		if (cast_with_count.containsKey(actor.getId())){
			int count =cast_with_count.get(actor.getId());
			cast_with_count.put(actor.getId(),count+1);
		}else{
			cast_with_count.put(actor.getId(),1);
		}
	}
	private void addIdstoMap(List<Film> films,Map<Integer,Integer> films_with_rate){
		for (Film film:films){
			if (films_with_rate.containsKey(film.getId())){
				int rate=films_with_rate.get(film.getId()).intValue();
				films_with_rate.put(film.getId(),rate+1);
			} else {
				films_with_rate.put(film.getId(), 1);
			}
		}
	}

	private void addKeyword(Keyword keyword,Map<Integer,Integer> keywords_with_count) {
		if (keywords_with_count.containsKey(keyword.getId())){
			int count =keywords_with_count.get(keyword.getId());
			keywords_with_count.put(keyword.getId(),count+1);
		}else{
			keywords_with_count.put(keyword.getId(),1);
		}		
	}
	private void fillActorsByFilm(Film film,Map<Integer,Integer> cast_with_count){
		List<Actor> actors = new ArrayList<Actor>();
		try{
			actors=api.getActorsListById(film.getId());
		} catch (RestClientException ex) {
			if (ex.getMessage().contains("429")) {
				try {
					TimeUnit.SECONDS.sleep(10);
					actors=api.getActorsListById(film.getId());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else {
				new ResponseEntity<>("Something went wrong", HttpStatus.EXPECTATION_FAILED);
			}
		}
		for (Actor actor : actors) {
			addBaseMovieCast(actor,cast_with_count);
		}	
	}
	private void fillKeywordsByFilm(Film film,Map<Integer,Integer> keywords_with_count){
		List<Keyword> keywords = new ArrayList<Keyword>();
		try {
			keywords = api.getListKeywordsById(film.getId());
		} catch (RestClientException e) {
			if (e.getMessage().contains("429")) {
				try {
					TimeUnit.SECONDS.sleep(10);
					keywords = api.getListKeywordsById(film.getId());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		for (Keyword word : keywords) {
			addKeyword(word, keywords_with_count);
		}
	}
	@Async
	public Future<String> fillMapByCast(List<Film> baseFilms,Map<Integer,Integer> films_with_rate){
		Map<Integer,Integer> cast_with_count = new HashMap<Integer, Integer>();
		for (Film film:baseFilms){
			fillActorsByFilm(film,cast_with_count);
		}
		if (cast_with_count.isEmpty()) return new AsyncResult<String>("Maps update by zero actors.");
		cast_with_count.forEach((id_actor,value)->{
			int total_pages=-1;
			for (int page=1;page<PAGE_ACTOR;page++){
				if (page>total_pages && total_pages!=-1) break;             
				try {
					ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_people="+id_actor);
					if (total_pages==-1) total_pages=response.getBody().getTotalPages();
					addIdstoMap(response.getBody().getResults(),films_with_rate);
				} catch (RestClientException e) {
					if (e.getMessage().contains("429")){
						try {
							System.out.println("Thread actors:Wait for 10 seconds");
							TimeUnit.SECONDS.sleep(10);
							ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_people="+id_actor);
							if (total_pages==-1) total_pages=response.getBody().getTotalPages();
							addIdstoMap(response.getBody().getResults(),films_with_rate);
						}catch(Exception exp){}
					}
				}
			}
		});
		return new AsyncResult<String>("Maps update by actors.");	 
	}
	@Async
	public Future<String> fillMapByGenres(List<Film> baseFilms,Map<Integer,Integer> films_with_rate){
		for (Film film :baseFilms){
			List<Genre> genres=film.getGenres();
			for (Genre genre:genres){
				int total_pages=-1;
				for (int page=1;page<PAGE_GENRES;page++){
					if (page>total_pages && total_pages!=-1) break;					
					try {
						ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_genres="+genre.getId());
						if (total_pages==-1) total_pages=response.getBody().getTotalPages();
						addIdstoMap(response.getBody().getResults(),films_with_rate);
					} catch (RestClientException e) {
						if (e.getMessage().contains("429")){
							try {
								System.out.println("Thread genres:Wait for 10 seconds");
								TimeUnit.SECONDS.sleep(10);
								ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_genres="+genre.getId());
								if (total_pages==-1) total_pages=response.getBody().getTotalPages();
								addIdstoMap(response.getBody().getResults(),films_with_rate);
							}	catch(Exception ex){}
						}
					}
				}
			}
		}
		return new AsyncResult<String>("Maps update by genres.");
	}
	@Async
	public Future<String> fillMapByKeywords(List<Film> baseFilms,Map<Integer,Integer> films_with_rate){
		Map<Integer,Integer> keywords_with_count = new HashMap<Integer, Integer>();
		for (Film film:baseFilms){
			fillKeywordsByFilm(film,keywords_with_count);
		}	
		if (keywords_with_count.isEmpty()) return new AsyncResult<String>("Maps update by zero keywords");			
		keywords_with_count.forEach((id,value)->{
			int total_pages=-1;
			for (int page=1;page<PAGE_KEYWORDS;page++){
				if (page>total_pages && total_pages!=-1) break;
				try{
					ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_keywords="+id);
					if (total_pages==-1) total_pages=response.getBody().getTotalPages();
					addIdstoMap(response.getBody().getResults(),films_with_rate);		
				}catch(RestClientException e){
					if (e.getMessage().contains("429")){
						try {
							System.out.println("Thread keywords:Wait for 10 seconds");
							TimeUnit.SECONDS.sleep(10);
							ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_keywords="+id);
							if (total_pages==-1) total_pages=response.getBody().getTotalPages();
							addIdstoMap(response.getBody().getResults(),films_with_rate);		
						}catch(Exception exp){}
					}
				}
			}
		});
		return new AsyncResult<String>("Maps update by keywords");
	}
}
