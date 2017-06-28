package repo.crawler.service.crawler;

import java.util.List;

import repo.crawler.entities.Repository;

public interface RepositoryCrawlerService<T extends Repository> {
	
	/**
	 * 
	 * Method called to manually map a Json to an Object of type {@link T} 
	 * 
	 * @param url The URL to call returning the Json.
	 * @return The mapped Object of type {@link T}
	 */
	T processDataFromJsonAsObject(String url);
	
	/**
	 * 
	 * Method called to manually map a Json to a List of Objects of type {@link T} 
	 * 
	 * @param url The URL to call returning the Json.
	 * @return The List of mapped type {@link T} Objects
	 */
	List<T> processDataFromJsonAsList(String url);
}
