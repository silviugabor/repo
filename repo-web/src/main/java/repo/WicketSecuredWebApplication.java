package repo;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.PackageResourceGuard;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;
import org.apache.wicket.settings.RequestCycleSettings.RenderStrategy;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.time.Duration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

import com.giffing.wicket.spring.boot.starter.app.WicketBootSecuredWebApplication;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ThemeProvider;
import de.agilecoders.wicket.less.BootstrapLess;
import de.agilecoders.wicket.less.LessResourceReference;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import de.agilecoders.wicket.webjars.WicketWebjars;
import de.agilecoders.wicket.webjars.settings.WebjarsSettings;
import repo.web.application.SpringSecurityAtuhenticatedWebSession;
import repo.web.wicket.AbstractWebPage;

@SpringBootApplication(scanBasePackages = {"repo"})
public class WicketSecuredWebApplication extends WicketBootSecuredWebApplication {

	private static final String BASE_PACKAGE_FOR_PAGES =  AbstractWebPage.class.getPackage().getName();
	private static final String DEFAULT_ENCODING = "UTF-8";
	
	@Override
	protected void init() {
		super.init();
		registerSpringComponentInjector();
		initPageMounting();
		initBootstrap();
		initWebjars();
		
		get().getResourceSettings().setPackageResourceGuard(new PackageResourceGuard());

		getMarkupSettings().setDefaultMarkupEncoding(DEFAULT_ENCODING);
		getRequestCycleSettings().setResponseRequestEncoding(DEFAULT_ENCODING);
		getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);
		
		if (getConfigurationType().equals(RuntimeConfigurationType.DEVELOPMENT)) {
			getMarkupSettings().setStripWicketTags(true);
			getMarkupSettings().setStripComments(true);
			getMarkupSettings().setCompressWhitespace(true);

			getDebugSettings().setAjaxDebugModeEnabled(true);
			getDebugSettings().setComponentUseCheck(true);
			getDebugSettings().setDevelopmentUtilitiesEnabled(true);

			getResourceSettings().setResourcePollFrequency(Duration.ONE_SECOND);
		}
	}
	
	@Override
	public Session newSession(Request request, Response response) {
		return new SpringSecurityAtuhenticatedWebSession(request);
	}

	@SuppressWarnings("unused")
	private void configureBootstrap() {

		final IBootstrapSettings settings = new BootstrapSettings();
		settings.useCdnResources(true);

		final ThemeProvider themeProvider = new BootswatchThemeProvider(BootswatchTheme.Spacelab);
		settings.setThemeProvider(themeProvider);

		//settings.setAutoAppendResources(true);
		
		Bootstrap.install(this, settings);
		BootstrapLess.install(this);
	}
	
	@Override
	protected ResourceReferenceRegistry newResourceReferenceRegistry() {
		return new ResourceReferenceRegistry(new LessRRR());
	}

	private static class LessRRR extends ResourceReferenceRegistry.DefaultResourceReferenceFactory {

		@Override
		public ResourceReference create(ResourceReference.Key key) {
			if (key.getName().endsWith(".less")) {
				return new LessResourceReference(key);
			} else {
				return super.create(key);
			}
		}
	}
	
	private void initWebjars() {
		WebjarsSettings settings = new WebjarsSettings();
		WicketWebjars.install(this, settings);
	}

	private void registerSpringComponentInjector() {
		getComponentInstantiationListeners().add(new SpringComponentInjector(this, getContext(), true));
	}

	private void initPageMounting() {
		// Hint from:
		// http://blog.xebia.com/2008/10/09/readable-url%E2%80%99s-in-wicket-an-introduction-to-wicketstuff-annotation/
		new AnnotatedMountScanner().scanPackage(BASE_PACKAGE_FOR_PAGES).mount(this);
	}


	private void initBootstrap() {	
		final ThemeProvider themeProvider = new BootswatchThemeProvider(BootswatchTheme.Cerulean);
		BootstrapSettings settings = new BootstrapSettings();
		settings.setJsResourceFilterName("footer-container").setThemeProvider(themeProvider);
		Bootstrap.install(this, settings);
		BootstrapLess.install(this);
	}


	@Override
	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
		return SpringSecurityAtuhenticatedWebSession.class;
	}

	protected ApplicationContext getContext() {
		return WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
	}
	
	public static void main(String[] args) throws Exception{
		new SpringApplicationBuilder().sources(WicketSecuredWebApplication.class).run(args);
	}
}
