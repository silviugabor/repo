package repo.crawler.service.crawler.github;

import repo.crawler.service.observer.ServiceObserver;

public interface ObservableService {
	void setServiceObserver(ServiceObserver serviceObserver);
	void removeServiceObserver();
	String notifyServiceObserverForTokenUpdate();
}
