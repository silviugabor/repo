package repo.web.application.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import repo.web.application.security.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long>{
}
