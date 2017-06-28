package repo.web.application.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	private static final Logger LOG = LoggerFactory.getLogger(SecurityServiceImpl.class);
	
	@Override
	public String findLoggedInUsername() {
		
		Object userDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();
		if (userDetails instanceof UserDetails)
			return ((UserDetails)userDetails).getUsername();
		return null;
	}

	@Override
	public boolean autologin(String username, String password) {
		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

			authenticationManager.authenticate(authenticationToken);

			if (authenticationToken.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
				LOG.info(String.format("Auto login %s successfully!", username));
				return true;
			}
			else
			{
				LOG.info("User '{}' failed to login. Reason: Bad credentials maybe?", username);
				return false;
			}
		} catch (AuthenticationException e) {
			LOG.info("User '{}' failed to login. Reason: {}", username, e.getMessage());
			return false;
		}
	}

}
