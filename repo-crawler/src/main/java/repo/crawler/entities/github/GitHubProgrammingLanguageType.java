package repo.crawler.entities.github;

import java.io.Serializable;

public enum GitHubProgrammingLanguageType implements Serializable {
	UNKNOWN("unknown", -42),
	JAVA("java", 1),
	JAVASCRIPT("javascript", 2),
	PYTHON("python", 3),
	C("c", 4),
	PHP("php", 5),
	RUBY("ruby", 6),
	SWIFT("swift", 7),
	SCALA("scala", 8),
	HTML("html", 9),
	CSS("css", 10),
	CPlusPlus("c++", 11);

	private String programmingLanguage;
	private Integer value;
	
	private GitHubProgrammingLanguageType(String programmingLanguage, Integer value) {
		this.programmingLanguage = programmingLanguage;
		this.value = value;
	}
	
	public String programmingLanguage(){
		return programmingLanguage;
	}
	
	public Integer value() {
		return value;
	}
}
