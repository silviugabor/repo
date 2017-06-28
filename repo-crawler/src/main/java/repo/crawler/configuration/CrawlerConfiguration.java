package repo.crawler.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import repo.crawler.entities.github.GitHubRepository;
import repo.crawler.service.crawler.RepositoryCrawlerService;
import repo.crawler.service.crawler.github.GitHubRepositoryCrawlerServiceImpl;
import repo.crawler.service.observer.ServiceObserver;
import repo.crawler.service.observer.token.TokenChangeServiceObserver;

@Configuration
public class CrawlerConfiguration {

	@Bean
	public ServiceObserver tokenChangeServiceObserver(){
		return new TokenChangeServiceObserver("access_token.txt");
	}
	
	@Bean
	public RepositoryCrawlerService<GitHubRepository> gitHubRepositoryCrawlerService(ServiceObserver tokenChangeServiceObserver){
		return new GitHubRepositoryCrawlerServiceImpl(tokenChangeServiceObserver);
	}
}
