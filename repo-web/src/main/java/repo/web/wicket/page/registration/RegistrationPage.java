package repo.web.wicket.page.registration;

import java.util.HashSet;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.Model;
import org.wicketstuff.annotation.mount.MountPath;

import repo.web.application.security.model.User;
import repo.web.application.security.spring.Roles;
import repo.web.wicket.HeaderFooter;

@MountPath("/registration")
@AuthorizeInstantiation(Roles.ANONYMOUS)
public class RegistrationPage extends HeaderFooter {

	private static final long serialVersionUID = 1L;

	@Override
	protected void onInitialize() {
		super.onInitialize();
		User user = new User();
		user.setRoles(new HashSet<>());
		add(new RegistrationPanel("registrationPanel", Model.of(user)));
		
		add(createPageTitleTag("registration.title"));
		oxygenCarousel.setVisible(false);
//        add(createPageHeading("registration.heading"));
//        add(createPageMessage("registration.message"));
	}
}
