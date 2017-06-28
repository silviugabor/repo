package repo.web.application.security.service;

public interface SecurityService {
	String findLoggedInUsername();

	boolean autologin(String username, String password);
}
