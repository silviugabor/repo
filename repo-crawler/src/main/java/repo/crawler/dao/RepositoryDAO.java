package repo.crawler.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import repo.crawler.entities.Repository;

@NoRepositoryBean
public interface RepositoryDAO<T extends Repository> extends JpaRepository<T, Long>{
	/**
	 * Method findByRepositoryName
	 * 
	 * @param name the repository name
	 * @return the repository having the passed name or null if no repository found
	 */
	public List<T> findByRepositoryName(String repositoryName);
}
