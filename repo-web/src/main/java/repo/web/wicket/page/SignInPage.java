package repo.web.wicket.page;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackBorder;
import org.apache.wicket.model.Model;
import org.wicketstuff.annotation.mount.MountPath;

import com.giffing.wicket.spring.boot.context.scan.WicketSignInPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.form.BootstrapForm;
import repo.web.application.SpringSecurityAtuhenticatedWebSession;
import repo.web.wicket.HeaderFooter;
import repo.web.wicket.behavior.PlaceholderBehavior;
import repo.web.wicket.behavior.RequiredBehavior;

@MountPath("/login")
@WicketSignInPage
public class SignInPage extends HeaderFooter {

	private static final long serialVersionUID = 1L;

	class LoginForm extends BootstrapForm<Void> {

		private static final long serialVersionUID = 1L;

		private TextField<String> usernameField;
		private TextField<String> passwordField;

		public void createAndAddToLoginFormUsernameAndPasswordFields() {
			add(new FormComponentFeedbackBorder("border")
					.add((usernameField = new RequiredTextField<String>("username", Model.of("")))));
			usernameField.add(new PlaceholderBehavior(getString("label.username")));
			usernameField.add(new RequiredBehavior());

			add(passwordField = new PasswordTextField("password", Model.of("")));
			passwordField.add(new PlaceholderBehavior(getString("label.password")));
			passwordField.add(new RequiredBehavior());
		}

		public LoginForm(String id) {
			super(id);
			IndicatingAjaxButton submit = new IndicatingAjaxButton("submit", Model.of("Submit")) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget target) {
					SpringSecurityAtuhenticatedWebSession session = SpringSecurityAtuhenticatedWebSession.getSpringSecurityAuthenticatedWebSession();
					if (session.signIn(LoginForm.this.usernameField.getModelObject(), LoginForm.this.passwordField.getModelObject())) {
						setResponsePage(getApplication().getHomePage());
					} else {
						target.add(LoginForm.this);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target) {
					target.add(LoginForm.this);
				}
			};
			add(submit);
		}
	}
	
	@Override
	protected void onInitialize() {
        super.onInitialize();

        add(createPageTitleTag("login.title"));
//        add(createPageHeading("login.heading"));
//        add(createPageMessage("login.message"));

        LoginForm loginForm = new LoginForm("loginForm");
        add(loginForm);
        loginForm.createAndAddToLoginFormUsernameAndPasswordFields();
    }

}
