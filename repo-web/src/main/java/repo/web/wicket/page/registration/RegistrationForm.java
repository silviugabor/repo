package repo.web.wicket.page.registration;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.agilecoders.wicket.core.markup.html.bootstrap.form.BootstrapForm;
import repo.web.application.security.model.User;
import repo.web.application.security.service.SecurityService;
import repo.web.application.security.service.UserService;

public class RegistrationForm extends BootstrapForm<User> {

	private static final long serialVersionUID = 1L;

	@SpringBean
	private UserService userService;
	
	@SpringBean
	private SecurityService securityService;
	
	public RegistrationForm(String id, IModel<User> model) {
		super(id, model);
	}
	
	@Override
	protected void onSubmit() {
		User user = getModelObject();
		userService.save(user);
		securityService.autologin(user.getUsername(), user.getPassword());
		setResponsePage(getApplication().getHomePage());
	}

}
