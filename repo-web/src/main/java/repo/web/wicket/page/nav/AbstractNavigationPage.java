package repo.web.wicket.page.nav;

import org.apache.wicket.markup.repeater.RepeatingView;

import repo.web.wicket.HeaderFooter;

public class AbstractNavigationPage extends HeaderFooter{

	private static final long serialVersionUID = 1L;
	protected RepeatingView listIcons;

	public AbstractNavigationPage() {
		super();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(createPageTitleTag("nav.title"));
//		add(createPageHeading("nav.title"));
//		add(createPageMessage("nav.message"));

		listIcons = new RepeatingView("listIcons");
		add(listIcons);
	}
}
