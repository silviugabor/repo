package repo.api.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import repo.crawler.entities.github.GitHubRepository;
import repo.spark.service.RepositoryMLService;

@Component
public class RepositoryService {
	private static final String REPOSITORY_ID_COLUMN_NAME = "id";
	private static final String REPOSITORY_DESCRIPTION_COLUMN_NAME = "description";
	
	@Autowired
	private RepositoryMLService repositoryMLService;
	
	@Value("${repository.kmeans.clustering.model.directory.path}")
	private String repositoryKMeansModelPath;
	
	public Map<String, Object> clusterizeRepositories(){
		return repositoryMLService.clusterizeRepositories(REPOSITORY_ID_COLUMN_NAME, REPOSITORY_DESCRIPTION_COLUMN_NAME, repositoryKMeansModelPath);
	}
	
	public Integer predictRepository(Long repositoryId, String repositoryDescription){
		return repositoryMLService.predictRepository(REPOSITORY_ID_COLUMN_NAME, REPOSITORY_DESCRIPTION_COLUMN_NAME, repositoryId, repositoryDescription, repositoryKMeansModelPath);
	}
	
	public List<GitHubRepository> getRecommmendations(Long repositoryId, String repositoryDescription, int maximumNumberOfRecommendations){
		GitHubRepository gitHubRepository = new GitHubRepository(new String(), repositoryDescription.getBytes(), Collections.emptyList());
		gitHubRepository.setId(repositoryId);
		return repositoryMLService.getRecommendationsFor(gitHubRepository, maximumNumberOfRecommendations, repositoryKMeansModelPath);
	}
}
