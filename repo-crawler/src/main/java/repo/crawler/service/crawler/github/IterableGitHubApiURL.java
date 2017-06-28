package repo.crawler.service.crawler.github;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IterableGitHubApiURL implements Iterable<String>{

	private int numberOfPages;
	private String mainURL;
	
	public IterableGitHubApiURL(String mainURL, int numberOfPages) {
		this.mainURL = mainURL;
		this.numberOfPages = numberOfPages;
	}
	
	@Override
	public Iterator<String> iterator() {
		return new ApiURLIterator(0, numberOfPages, mainURL);
	}
	
	private static final class ApiURLIterator implements Iterator<String>{

		private int cursor;
		private final int end;
		private String mainURL;
		
		public ApiURLIterator(int start, int end, String mainURL) {
			this.cursor = start;
			this.end = end;
			this.mainURL = mainURL;
		}
		
		@Override
		public boolean hasNext() {
			return cursor < end;
		}

		@Override
		public String next() {
			if (this.hasNext())
				return mainURL.concat("&page=" + (++cursor));
			throw new NoSuchElementException();
		}
		
	}

}
