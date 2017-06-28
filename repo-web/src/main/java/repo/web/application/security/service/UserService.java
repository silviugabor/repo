package repo.web.application.security.service;

import repo.web.application.security.model.User;

public interface UserService {
	void save(User user);

	User findByUsername(String username);
}
