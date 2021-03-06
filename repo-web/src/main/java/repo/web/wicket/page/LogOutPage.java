package repo.web.wicket.page;

import javax.servlet.http.Cookie;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.http.WebResponse;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.wicketstuff.annotation.mount.MountPath;

import repo.web.application.security.spring.Roles;

@MountPath("/logout")
@AuthorizeInstantiation(Roles.USER)
public class LogOutPage extends WebPage {

	private static final long serialVersionUID = 1L;

	@Override
    protected void onInitialize() {
        super.onInitialize();

        getSession().invalidate();
        removeRememberMeCookie();
        throw new RestartResponseException(SignInPage.class);
    }

    private void removeRememberMeCookie() {
        ((WebResponse)getResponse()).clearCookie(new Cookie(TokenBasedRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY, null));
    }
}