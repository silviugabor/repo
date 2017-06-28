package repo.web.application.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import repo.web.application.security.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByUsername(String username);
}
