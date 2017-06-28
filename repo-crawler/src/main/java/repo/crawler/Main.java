package repo.crawler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import repo.crawler.dao.GitHubRepositoryDAO;
import repo.crawler.entities.github.GitHubRepository;
import repo.crawler.service.crawler.RepositoryCrawlerService;

//@SpringBootApplication
public class Main implements CommandLineRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Autowired
	RepositoryCrawlerService<GitHubRepository> gitHubRepositoryCrawlerService;

	@Autowired
	GitHubRepositoryDAO gitHubRepositoryDAO;
	
	@Override
	public void run(String... args) throws Exception {
		List<GitHubRepository> gitHubRepositories = gitHubRepositoryCrawlerService.processDataFromJsonAsList("https://api.github.com/search/repositories?q=language:python");
		if (gitHubRepositories.size() > 0)
			LOG.info("This session has fetched " + gitHubRepositories.size() + " complete repositories.");
		else
			LOG.info("No repositories have been fetched this session.");
	}
}