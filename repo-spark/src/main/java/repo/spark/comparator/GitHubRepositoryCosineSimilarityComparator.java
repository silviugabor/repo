package repo.spark.comparator;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import repo.crawler.entities.github.GitHubRepository;

@Component
public class GitHubRepositoryCosineSimilarityComparator implements Comparator<GitHubRepository> {

	@Override
	public int compare(GitHubRepository o1, GitHubRepository o2) {
		return o1.getSimilarityScore() > o2.getSimilarityScore() ? -1 : (o1.getSimilarityScore() < o2.getSimilarityScore() ? 1 : 0);
	}

}
