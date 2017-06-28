package repo.crawler.entities.github;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.transaction.Transactional;

import repo.crawler.entities.Repository;

@Entity
@Transactional
@DiscriminatorValue(value = "github")
public class GitHubRepository extends Repository implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Lob
	@Column(name = "readme", columnDefinition = "LONGBLOB")
	@Basic(fetch = FetchType.LAZY, optional = false)
	private byte[] readme;

	@Lob
	@Column(name = "description", columnDefinition = "LONGBLOB")
	private byte[] description;
	
	
	@ElementCollection(targetClass = GitHubProgrammingLanguageType.class, fetch=FetchType.EAGER)
	@CollectionTable(
	        name = "repository_programming_languages", 
	        joinColumns = @JoinColumn(name = "id")
	)
	@Column(name = "language_id")
	private List<GitHubProgrammingLanguageType> programmingLanguageTypeList;

	@Column(name = "repository_link")
	private String repositoryLink;

	@Transient
	private Double similarityScore;

	public GitHubRepository(){
	}
	
	public GitHubRepository(String name, byte[] readme, byte[] description, List<GitHubProgrammingLanguageType> programmingLanguageTypeList) {
		this.setRepositoryName(name);
		this.readme = readme;
		this.description = description;
		this.programmingLanguageTypeList = programmingLanguageTypeList;
	}
	
	public GitHubRepository(String name, byte[] readme, byte[] description, String link, List<GitHubProgrammingLanguageType> programmingLanguageTypeList) {
		this(name, readme, description, programmingLanguageTypeList);
		this.repositoryLink = link;
	}
	
	public GitHubRepository(String name, byte[] description, List<GitHubProgrammingLanguageType> programmingLanguageTypeList) {
		this.setRepositoryName(name);
		this.description = description;
		this.programmingLanguageTypeList = programmingLanguageTypeList;
	}

	public byte[] getReadme() {
		return readme;
	}

	public void setReadme(byte[] readme) {
		this.readme = readme;
	}

	public byte[] getDescription() {
		return description;
	}

	public void setDescription(byte[] description) {
		this.description = description;
	}

	public List<GitHubProgrammingLanguageType> getProgrammingLanguageTypeList() {
		return programmingLanguageTypeList;
	}

	public void setProgrammingLanguageTypeList(List<GitHubProgrammingLanguageType> programmingLanguageTypeList) {
		this.programmingLanguageTypeList = programmingLanguageTypeList;
	}

	public String getRepositoryLink() {
		return repositoryLink;
	}

	public void setRepositoryLink(String repositoryLink) {
		this.repositoryLink = repositoryLink;
	}
	
	public Double getSimilarityScore() {
		return similarityScore;
	}

	public void setSimilarityScore(Double similarityScore) {
		this.similarityScore = similarityScore;
	}
	
	@Override
	public String toString() {
		return new String(getDescription());
	}
}
