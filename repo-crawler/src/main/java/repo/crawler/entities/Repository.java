package repo.crawler.entities;

import javax.persistence.*;


@Entity
@Table(name = "repository")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "REPOSITORY_TYPE")
public class Repository {
	
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;
	
	@Column(name = "repository_name")
	private String repositoryName;
	
	@Column(name = "cluster_number")
	private Integer clusterNumber;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
	
	public Integer getClusterNumber() {
		return clusterNumber;
	}

	public void setClusterNumber(Integer clusterNumber) {
		this.clusterNumber = clusterNumber;
	}
	
}
