package sp.advicer.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import sp.advicer.entity.dto.actor.Actor;
import sp.advicer.entity.dto.film.Film;
import sp.advicer.entity.dto.film.Genre;
import sp.advicer.entity.dto.keyword.Keyword;
import sp.advicer.entity.dto.responses.ResponseForResults;
import sp.advicer.repository.TmdbApi;


@Service
public class MovieServiceImpl implements MovieService {
	private Map<Integer,Integer> films_with_rate = new ConcurrentHashMap<Integer,Integer>();
	private List<Film> movies= new ArrayList<Film>();
	private Map<Integer,Integer> cast_with_count = new HashMap<Integer, Integer>();
	private Map<Integer,Integer> keywords_with_count = new HashMap<Integer, Integer>();
	private final int PAGE_GENRES=3;
	private final int PAGE_ACTOR=2;
	private final int PAGE_KEYWORDS=3;
	private TmdbApi api =new TmdbApi();
	private AtomicInteger count_finished_threads=new AtomicInteger(0);
	@Override
	public void addBaseMovie(Film film) {
		if (movies.contains(film)) return;
		movies.add(film);
		fillKeywordsByFilm(film);
		fillActorsByFilm(film);
	}
	public void addBaseMovieCast(Actor actor) {
		if (cast_with_count.containsKey(actor.getId())){
			int count =cast_with_count.get(actor.getId());
			cast_with_count.put(actor.getId(),count+1);
		}else{
			cast_with_count.put(actor.getId(),1);
		}
	}
	private void addIdstoMap(List<Film> films){
		for (Film film:films){
			if (films_with_rate.containsKey(film.getId())){
				int rate=films_with_rate.get(film.getId()).intValue();
				films_with_rate.put(film.getId(),rate+1);
			} else {
				films_with_rate.put(film.getId(), 1);
			}
		}
	}
	private void addKeyword(Keyword keyword) {
		if (keywords_with_count.containsKey(keyword.getId())){
			int count =keywords_with_count.get(keyword.getId());
			keywords_with_count.put(keyword.getId(),count+1);
		}else{
			keywords_with_count.put(keyword.getId(),1);
		}		
	}
	private void deleteBaseIdInMap(){
		for (Film film:movies){
			films_with_rate.remove(film.getId());
		}
	}
	private void fillActorsByFilm(Film film){
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
			addBaseMovieCast(actor);
		}	
	}
	private void fillKeywordsByFilm(Film film){
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
			addKeyword(word);
		}
	}
	private void fillMapByCast(){
		if (cast_with_count.isEmpty()){
			count_finished_threads.addAndGet(1);
			return;
		}
		cast_with_count.forEach((id_actor,value)->{
			int total_pages=-1;
			for (int page=1;page<PAGE_ACTOR;page++){
				if (page>total_pages && total_pages!=-1) break;             
				try {
					ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_people="+id_actor);
					if (total_pages==-1) total_pages=response.getBody().getTotalPages();
					addIdstoMap(response.getBody().getResults());
				} catch (RestClientException e) {
					if (e.getMessage().contains("429")){
						try {
							System.out.println("Thread actors:Wait for 10 seconds");
							TimeUnit.SECONDS.sleep(10);
							ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_people="+id_actor);
							if (total_pages==-1) total_pages=response.getBody().getTotalPages();
							addIdstoMap(response.getBody().getResults());
						}catch(Exception exp){}
					}
				}
			}
		});
		System.out.println("Maps update by actors.");
		count_finished_threads.addAndGet(1);
	}
	private void fillMapByGenres(){
		if (movies.isEmpty()){
			count_finished_threads.addAndGet(1);
			return;
		}
		for (Film film :movies){
			List<Genre> genres=film.getGenres();
			for (Genre genre:genres){
				int total_pages=-1;
				for (int page=1;page<PAGE_GENRES;page++){
					if (page>total_pages && total_pages!=-1) break;					
					try {
						ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_genres="+genre.getId());
						if (total_pages==-1) total_pages=response.getBody().getTotalPages();
						addIdstoMap(response.getBody().getResults());
					} catch (RestClientException e) {
						if (e.getMessage().contains("429")){
							try {
								System.out.println("Thread genres:Wait for 10 seconds");
								TimeUnit.SECONDS.sleep(10);
								ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_genres="+genre.getId());
								if (total_pages==-1) total_pages=response.getBody().getTotalPages();
								addIdstoMap(response.getBody().getResults());
							}	catch(Exception ex){}
						}
					}
				}
			}
		}
		count_finished_threads.addAndGet(1);
		System.out.println("Maps update by genres.");
	}

	private void fillMapByKeywords(){
		if (keywords_with_count.isEmpty()){
			count_finished_threads.addAndGet(1);
			return;
		}
		keywords_with_count.forEach((id,value)->{
			int total_pages=-1;
			for (int page=1;page<PAGE_KEYWORDS;page++){
				if (page>total_pages && total_pages!=-1) break;
				try{
					ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_keywords="+id);
					if (total_pages==-1) total_pages=response.getBody().getTotalPages();
					addIdstoMap(response.getBody().getResults());		
				}catch(RestClientException e){
					if (e.getMessage().contains("429")){
						try {
							System.out.println("Thread keywords:Wait for 10 seconds");
							TimeUnit.SECONDS.sleep(10);
							ResponseEntity<ResponseForResults> response = api.getResponseFromDiscover(page,"&with_keywords="+id);
							if (total_pages==-1) total_pages=response.getBody().getTotalPages();
							addIdstoMap(response.getBody().getResults());		
						}catch(Exception exp){}
					}
				}
			}
		});
		System.out.println("Maps update by keywords");
		count_finished_threads.addAndGet(1);
	}
	private List<Integer> getListIdFromMap(Integer number_of_films){
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
	public List<Integer> getRecomendationList(Integer number_of_films) {
		count_finished_threads.set(0);
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				System.out.println("Fill by genres running");
				fillMapByGenres();
			}
		});
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				System.out.println("Fill by cast running");
				fillMapByCast();
			}
		});
		Thread t3 = new Thread(new Runnable() {
			public void run() {
				System.out.println("Fill by keywords running");
				fillMapByKeywords();
			}
		});
		t1.start();
		t2.start();
		t3.start();
		while (count_finished_threads.get()!=3){ }
		deleteBaseIdInMap();
		return getListIdFromMap(number_of_films);
	}
	@Override
	public void prepare(){
		films_with_rate.clear();
		cast_with_count.clear();
		keywords_with_count.clear();
		movies.clear();
		System.out.println("Prepared");
	}
}
