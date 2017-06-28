package repo.api.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import repo.api.service.RepositoryService;
import repo.crawler.entities.github.GitHubRepository;


@RestController
@RequestMapping("/repository")
public class RepositoryController {
	private static final String CLUSTER_PREDICTION_KEY = "cluster";
	private static final String RECOMMENDATIONS_KEY = "recommendations";
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryController.class);
	
	@Autowired
	private RepositoryService repositoryService;
	
	@RequestMapping(value = "/train", method = RequestMethod.GET)
	ResponseEntity<Map<String, Object>> clusterizeRepositories(){
		return new ResponseEntity<>(repositoryService.clusterizeRepositories(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/predict", method = RequestMethod.GET)
	ResponseEntity<Map<String, Object>> predictRepository(
			@RequestParam(value = "id", defaultValue = "1234") Long repositoryId,
			@RequestParam(value = "description", defaultValue = "Description sample") String repositoryDescription){
		
		Integer clusterPrediction = repositoryService.predictRepository(repositoryId, repositoryDescription);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put(CLUSTER_PREDICTION_KEY, clusterPrediction);
		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/recommend", method = RequestMethod.GET)
	ResponseEntity<Map<String, Object>> getRecommendations(
			@RequestParam(value = "id", defaultValue = "1234") Long repositoryId,
			@RequestParam(value = "description", defaultValue = "Description sample") String repositoryDescription,
			@RequestParam(value = "recommendations", defaultValue = "10") int maximumNumberOfRecommendations){
		
		List<GitHubRepository> recommendedGitHubRepositories = repositoryService.getRecommmendations(repositoryId, repositoryDescription, maximumNumberOfRecommendations);
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put(RECOMMENDATIONS_KEY, recommendedGitHubRepositories);
		return new ResponseEntity<>(responseMap, HttpStatus.OK);
	}
	
	@ExceptionHandler(Exception.class)
	public ModelAndView handleError(HttpServletRequest req, Exception ex) {
		LOG.error("Request: " + req.getRequestURL() + " raised " + ex);

		ModelAndView mav = new ModelAndView();
		mav.addObject("exception", ex);
		mav.addObject("url", req.getRequestURL());
		mav.setViewName("error");
		return mav;
	}

}
