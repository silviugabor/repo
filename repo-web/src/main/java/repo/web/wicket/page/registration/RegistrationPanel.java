package repo.web.wicket.page.registration;

import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import repo.web.application.security.model.User;
import repo.web.wicket.validator.DatabaseValidator;

public class RegistrationPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private IModel<User> userModel;
	
	public RegistrationPanel(String id, IModel<User> model) {
		super(id, model);
		this.userModel = model;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final TextField<String> usernameTextField = new RequiredTextField<>("username", new PropertyModel<>(userModel.getObject(), "username"));
		final TextField<String> passwordTextField = new PasswordTextField("password", new PropertyModel<>(userModel.getObject(), "password"));
		final TextField<String> confirmPasswordTextField = new PasswordTextField("confirmPassword", new PropertyModel<>(userModel.getObject(), "passwordConfirm"));
		usernameTextField.setRequired(true);
		passwordTextField.setRequired(true);
		confirmPasswordTextField.setRequired(true);
		
		final RegistrationForm registrationForm = new RegistrationForm("registrationForm", userModel);
		registrationForm.add(usernameTextField, passwordTextField, confirmPasswordTextField);
		registrationForm.add(DatabaseValidator.findByProperty(User.class, "username"));
		add(registrationForm);
	}

}
