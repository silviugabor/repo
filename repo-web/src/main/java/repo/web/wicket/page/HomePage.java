package repo.web.wicket.page;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;

import repo.web.application.security.spring.Roles;
import repo.web.wicket.HeaderFooter;

@WicketHomePage
@AuthorizeInstantiation({Roles.USER})
public class HomePage extends HeaderFooter {

	private static final long serialVersionUID = 1L;

	public HomePage() {
		super();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		oxygenCarousel.setVisible(false);
		add(createPageTitleTag("nav.title"));
	}
}
