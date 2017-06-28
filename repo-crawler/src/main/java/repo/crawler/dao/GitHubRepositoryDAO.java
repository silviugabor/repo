package repo.crawler.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import repo.crawler.entities.github.GitHubRepository;

public interface GitHubRepositoryDAO extends RepositoryDAO<GitHubRepository> {
	@Query("SELECT r FROM GitHubRepository r WHERE r.repositoryName LIKE %:name%")
	public List<GitHubRepository> findByRepositoryNameLike(@Param("name")String name);
	
	@Query("SELECT r FROM GitHubRepository r WHERE r.description LIKE %:description%")
	public List<GitHubRepository> findByDescriptionLike(@Param("description")String description);
	
	@Query("SELECT r FROM GitHubRepository r WHERE r.readme LIKE %:readme%")
	public List<GitHubRepository> findByReadmeLike(@Param("readme")String readme);
	
	public List<GitHubRepository> findByClusterNumber(Integer clusterNumber);
	
	public List<GitHubRepository> findByRepositoryName(String repositoryName);
}
