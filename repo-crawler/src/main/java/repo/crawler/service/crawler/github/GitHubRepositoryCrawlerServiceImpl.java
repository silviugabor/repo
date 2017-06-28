package repo.crawler.service.crawler.github;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import repo.crawler.dao.GitHubRepositoryDAO;
import repo.crawler.entities.github.GitHubProgrammingLanguageType;
import repo.crawler.entities.github.GitHubRepository;
import repo.crawler.service.crawler.RepositoryCrawlerService;
import repo.crawler.service.observer.ServiceObserver;
import repo.crawler.utils.EnumUtils;
import repo.crawler.utils.GsonUtils;

public class GitHubRepositoryCrawlerServiceImpl implements RepositoryCrawlerService<GitHubRepository>, ObservableService {

	private static final Logger LOG = LoggerFactory.getLogger(GitHubRepositoryCrawlerServiceImpl.class);
	private ServiceObserver serviceObserver;
	private String currentToken;
	private Long repositoryIndex = 0L;
	 
	@Autowired
	private GitHubRepositoryDAO gitHubRepositoryDao;
	
	public GitHubRepositoryCrawlerServiceImpl(ServiceObserver serviceObserver) {
		this.serviceObserver = serviceObserver;
	}
	
	@Override
	public GitHubRepository processDataFromJsonAsObject(String url) {
		return null;
	}

	@Override
	public List<GitHubRepository> processDataFromJsonAsList(String url) {
		List<GitHubRepository> gitHubRepositories = new ArrayList<>();
		currentToken = notifyServiceObserverForTokenUpdate();
		IterableGitHubApiURL iterableGitHubApiURL = new IterableGitHubApiURL(url, 30);

		iterableGitHubApiURL.forEach((apiUrl) -> {
			try {

				JsonArray jsonArray;
				JsonObject jsonObject = GsonUtils.readObjectFromJsonUrl(apiUrl, JsonObject.class);
				jsonArray = jsonObject.getAsJsonArray("items");
				for (JsonElement jsonElement : jsonArray) {
					// get name
					String repositoryName = jsonElement.getAsJsonObject().get("name").getAsString();

					// get description
					String repositoryDescription = jsonElement.getAsJsonObject().get("description").isJsonNull() == false ? jsonElement.getAsJsonObject().get("description").getAsString() : "";

					// get readme.md
					String repositoryContentsUrl = jsonElement.getAsJsonObject().get("contents_url").getAsString().replaceAll("\\{\\+path\\}", "");
					Thread.sleep(1700L);
					String repositoryReadme = getGitHubReadme(addTokenToUrl(repositoryContentsUrl));

					// get link
					String repositoryLink = jsonElement.getAsJsonObject().get("svn_url").getAsString();
					
					// get programming languages
					String repositoryLanguagesUrl = jsonElement.getAsJsonObject().get("languages_url").getAsString();
					Thread.sleep(1700L);
					List<GitHubProgrammingLanguageType> repositoryGitHubProgrammingLanguageTypes = getGitHubProgrammingLanguageTypes(addTokenToUrl(repositoryLanguagesUrl));

					if (Strings.isNullOrEmpty(repositoryReadme) ||  Strings.isNullOrEmpty(repositoryDescription) || repositoryGitHubProgrammingLanguageTypes == null
							|| repositoryGitHubProgrammingLanguageTypes.size() == 0)
						continue;

					// add GitHub repository to list
					GitHubRepository gitHubRepository = new GitHubRepository(repositoryName,
							repositoryReadme.getBytes(), repositoryDescription.getBytes(), repositoryLink,
							repositoryGitHubProgrammingLanguageTypes);

					if (!checkIfGitHubRepositoryExist(gitHubRepository)) {
						GitHubRepository savedGitHubRepository = gitHubRepositoryDao.saveAndFlush(gitHubRepository);
						if (savedGitHubRepository != null)
							LOG.info("Repository no " + (++repositoryIndex) + " with name "
									+ gitHubRepository.getRepositoryName() + " has been successfully saved.");
						gitHubRepositories.add(gitHubRepository);
					} else {
						LOG.info("Repository with name " + gitHubRepository.getRepositoryName()
								+ " already exists and has been skipped.");
					}

					// change token after current request
					currentToken = notifyServiceObserverForTokenUpdate();
				}
			} catch (IOException e) {
				LOG.debug("Error opening stream for URL " + apiUrl, e);
			} catch (InterruptedException e) {
				LOG.debug("Error sleeping current thread.", e);
			} catch (Exception e) {
				LOG.debug("Unknown exception occured.", e);
			}
		});
		return gitHubRepositories;
	}

	/**
	 * 
	 * Method to get the README file (as String) from a GitHub repository
	 * 
	 * @param contentsUrl The GitHub API URL that refers to the repository contents
	 * @return The README file as a String object if the file is found or null if no README file has been found
	 * @throws IOException Exception thrown if the given {@link contentsUrl} is invalid.
	 */
	private String getGitHubReadme(String contentsUrl) throws IOException {
		JsonArray contentsArray = GsonUtils.readObjectFromJsonUrl(contentsUrl, JsonArray.class);
		for (JsonElement contentJsonElement : contentsArray) {
			JsonObject contentJsonObject = contentJsonElement.getAsJsonObject(); //get whole content item
			if (!contentJsonObject.isJsonNull() && contentJsonObject.has("name")){
				JsonElement contentNameJsonElement = contentJsonObject.get("name");
				String contentName = contentNameJsonElement.getAsString().toLowerCase();
				if (contentName.contains("readme"))
				{
					String repositoryReadmeDownloadUrl = contentJsonObject.get("download_url").getAsString();
					String rawReadme = IOUtils.toString(new URL(addTokenToUrl(repositoryReadmeDownloadUrl)), "UTF-8");
					if (rawReadme != null)
						return Jsoup.parse(rawReadme).text();
					else
						return rawReadme;
				}
			}
		}
		return null;
	}
	
	private boolean checkIfGitHubRepositoryExist(GitHubRepository gitHubRepository) throws NoSuchAlgorithmException{
		List<GitHubRepository> gitHubRepositories = gitHubRepositoryDao.findByRepositoryName(gitHubRepository.getRepositoryName());
		if (gitHubRepositories != null && gitHubRepositories.size() > 0){
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(gitHubRepository.getDescription());
			messageDigest.update(gitHubRepository.getReadme());
			byte[] originalGitHubRepositoryReadmeAndDescriptionSHA256Hash = messageDigest.digest();
			
			for (GitHubRepository gHubRepository : gitHubRepositories){
				messageDigest.reset();
				messageDigest.update(gHubRepository.getDescription());
				messageDigest.update(gHubRepository.getReadme());
				byte[] currentGitHubRepositoryReadmeAndDescriptionSHA256Hash = messageDigest.digest();
				if (Arrays.equals(originalGitHubRepositoryReadmeAndDescriptionSHA256Hash, currentGitHubRepositoryReadmeAndDescriptionSHA256Hash))
					return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * 
	 * Method to get the list of programming languages of a GitHub repository.
	 * 
	 * @param url The GitHub API repository languages URL
	 * @return A list of programming languages of the repository in cause
	 * @throws IOException Exception thrown if the given languages URL is invalid or if the connection could not be made
	 */
	private List<GitHubProgrammingLanguageType> getGitHubProgrammingLanguageTypes(String url) throws IOException {
		List<GitHubProgrammingLanguageType> gitHubProgrammingLanguageTypes = new ArrayList<>();
		JsonObject jsonObject = GsonUtils.readObjectFromJsonUrl(url, JsonObject.class);
		if (jsonObject != null) {
			jsonObject.entrySet().forEach(mapEntry -> {
				GitHubProgrammingLanguageType gitHubProgrammingLanguageType = EnumUtils.getEnumFromString(GitHubProgrammingLanguageType.class, mapEntry.getKey());
				if (gitHubProgrammingLanguageType != null)
					gitHubProgrammingLanguageTypes.add(gitHubProgrammingLanguageType);

			});
			return gitHubProgrammingLanguageTypes;
		}
		return null;
	}

	private String addTokenToUrl(String url){
		return url + "?access_token=" + currentToken;
	}
	
	public void setServiceObserver(ServiceObserver serviceObserver) {
		this.serviceObserver = serviceObserver;
	}

	public void removeServiceObserver() {
		this.serviceObserver = null;
	}

	public String notifyServiceObserverForTokenUpdate() {
		return serviceObserver.updateToken();
	}
}
