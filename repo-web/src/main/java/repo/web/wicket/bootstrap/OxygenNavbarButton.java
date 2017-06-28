package repo.web.wicket.bootstrap;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;

public class OxygenNavbarButton<T> extends NavbarButton<T> {

	private static final long serialVersionUID = 1L;

	public <P extends Page> OxygenNavbarButton(Class<P> pageClass, IModel<String> label) {
		super(pageClass, label);
	}

	public <P extends Page> OxygenNavbarButton(Class<P> pageClass, IconType iconType) {
		super(pageClass, iconType);
	}

	public <P extends Page> OxygenNavbarButton(Class<P> pageClass, PageParameters parameters, IModel<String> label) {
		super(pageClass, parameters, label);
	}
	
	@Override
	public boolean isActive(Component button) {
        return false;
	}
}
