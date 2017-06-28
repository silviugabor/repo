package repo.web.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.Request;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import repo.web.application.security.service.SecurityService;

public class SpringSecurityAtuhenticatedWebSession extends AuthenticatedWebSession {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityAtuhenticatedWebSession.class);

	@SpringBean(name = "authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	
	@SpringBean
	private SecurityService securityService;

	private HttpSession httpSession;

	public SpringSecurityAtuhenticatedWebSession(Request request) {
		super(request);
		Injector.get().inject(this);
		this.httpSession = ((HttpServletRequest) request.getContainerRequest()).getSession();
//		if (authenticationManager == null) {
//			throw new IllegalStateException("Injection of AuthenticationManager failed.");
//		}
		if (securityService == null) {
			throw new IllegalStateException("Injection of SecurityService failed.");
		}
	}

	@Override
	protected boolean authenticate(String username, String password) {
//		boolean authenticated;
//        try {
//            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//            SecurityContextHolder.getContext().setAuthentication(authentication);      
//        	httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
//            authenticated = authentication.isAuthenticated();
//        } catch (AuthenticationException e) {
//            LOG.warn("User '{}' failed to login. Reason: {}", username, e.getMessage());
//            authenticated = false;
//        }
//        return authenticated;
		httpSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
		LOG.info("Authenticating '{}'..", username);
		return securityService.autologin(username, password);
	}
	

	@Override
    public Roles getRoles() {
        Roles roles = new Roles();
        if (isSignedIn()) {
        	getRolesIfSignedIn(roles);
        }
        else{
        	roles.add(repo.web.application.security.spring.Roles.ANONYMOUS);
        }
        return roles;
    }

    private void getRolesIfSignedIn(Roles roles) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();                        
            addRolesFromAuthentication(roles, authentication);
    }

    private void addRolesFromAuthentication(Roles roles, Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority().replaceFirst("ROLE_",""));
        }
    }

	public static SpringSecurityAtuhenticatedWebSession getSpringSecurityAuthenticatedWebSession() {
		return (SpringSecurityAtuhenticatedWebSession) Session.get();
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}

}
