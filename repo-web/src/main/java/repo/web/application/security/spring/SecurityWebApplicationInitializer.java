package repo.web.application.security.spring;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@Order(2)
public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {
	
	private static final Logger LOG = LoggerFactory.getLogger(SecurityWebApplicationInitializer.class);
	
	@Override
	protected EnumSet<DispatcherType> getSecurityDispatcherTypes() {
		 EnumSet<DispatcherType> securityDispatcherTypes = super.getSecurityDispatcherTypes();
		 securityDispatcherTypes.add(DispatcherType.FORWARD);
		 securityDispatcherTypes.add(DispatcherType.ASYNC);
		 securityDispatcherTypes.add(DispatcherType.REQUEST);
		return securityDispatcherTypes;
	}
	
	@Override
	protected void afterSpringSecurityFilterChain(ServletContext servletContext) {
		LOG.info("afterSpringSecurityFilterChain");
		super.afterSpringSecurityFilterChain(servletContext);
	}
	
}