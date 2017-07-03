package repo.web.wicket;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.core.io.ClassPathResource;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.core.markup.html.bootstrap.carousel.CarouselImage;
import de.agilecoders.wicket.core.markup.html.bootstrap.carousel.ICarouselImage;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.GlyphIconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarComponents;
import de.agilecoders.wicket.core.util.Attributes;
import repo.web.application.security.spring.Roles;
import repo.web.wicket.bootstrap.OxygenCarousel;
import repo.web.wicket.bootstrap.OxygenNavbarButton;
import repo.web.wicket.page.HomePage;
import repo.web.wicket.page.LogOutPage;
import repo.web.wicket.page.RecommendationsPage;
import repo.web.wicket.page.SignInPage;
import repo.web.wicket.page.registration.RegistrationPage;

public abstract class HeaderFooter extends AbstractWebPage {

	private static final long serialVersionUID = 1L;
	
	private CssClassNameAppender scrollClassAppender;
	protected OxygenCarousel oxygenCarousel;

	@Override
	protected void onInitialize() {
		super.onInitialize();
		scrollClassAppender = new CssClassNameAppender("scroll");
		add(createNavbar());
		try{
			oxygenCarousel = createCarouselSlider();
			oxygenCarousel.setOutputMarkupPlaceholderTag(true);
			add(oxygenCarousel);
		}catch (IOException e){
			System.out.println("Nooooo");
		}
		
		add(createFooterHomePageLink());
	}

	public HeaderFooter() {
		super();
	}

	public HeaderFooter(PageParameters parameters) {
		super(parameters);
	}

	protected void addHomePageButton(Navbar navbar) {
		NavbarButton<HomePage> navbarButton = new OxygenNavbarButton<HomePage>(this.getApplication().getHomePage(), Model.of("Home")).setIconType(GlyphIconType.home);
		navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.RIGHT, navbarButton));
		MetaDataRoleAuthorizationStrategy.authorize(navbarButton, Component.RENDER, Roles.ANONYMOUS);
	}

	protected void addLogoutButton(Navbar navbar) {
		NavbarButton<LogOutPage> navbarButton = new OxygenNavbarButton<LogOutPage>(LogOutPage.class, Model.of("Logout")).setIconType(GlyphIconType.off);
		navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.RIGHT, navbarButton));
		MetaDataRoleAuthorizationStrategy.authorize(navbarButton, Component.RENDER, Roles.USER);
	}
	
	protected void addRegisterButton(Navbar navbar){
		NavbarButton<RegistrationPage> navbarButton = new OxygenNavbarButton<RegistrationPage>(RegistrationPage.class, Model.of("Register")).setIconType(GlyphIconType.exclamationsign);
		navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.RIGHT, navbarButton));
		MetaDataRoleAuthorizationStrategy.authorize(navbarButton, Component.RENDER, Roles.ANONYMOUS);
	}
	
	protected void addRecommendationsPageButton(Navbar navbar) {
		NavbarButton<RecommendationsPage> navbarButton = new OxygenNavbarButton<RecommendationsPage>(RecommendationsPage.class, Model.of("Recommendations")).setIconType(GlyphIconType.handup);
		navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.RIGHT, navbarButton));
		MetaDataRoleAuthorizationStrategy.authorize(navbarButton, Component.RENDER, Roles.USER);
	}
	
	protected void addLoginButton(Navbar navbar) {
		NavbarButton<SignInPage> navbarButton = new OxygenNavbarButton<SignInPage>(SignInPage.class, Model.of("Login")).setIconType(GlyphIconType.login);
		navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.RIGHT, navbarButton));
		MetaDataRoleAuthorizationStrategy.authorize(navbarButton, Component.RENDER, Roles.ANONYMOUS);
	}

	protected Link<Void> createFooterHomePageLink(){
		Link<Void> homepageLink = new Link<Void>("homePageLink"){

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(getApplication().getHomePage());
			}
			
		};
		return homepageLink;
	}

	protected Navbar createNavbar() {
		Navbar navbar = new Navbar("navbar"){
			
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newNavigation(String componentId, IModel<List<Component>> listModel) {
				return new ListView<Component>(componentId, listModel) {
		            
					private static final long serialVersionUID = 1L;

					@Override
		            protected void populateItem(ListItem<Component> components) {
		                Component component = components.getModelObject();
		                components.add(component);
		                components.add(scrollClassAppender);
		            }
				};
			}
			
			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.remove("role");
				tag.remove("class");
				Attributes.addClass(tag, "main-nav");
			}
		};
		navbar.setBrandName(Model.of("Repository Recommendations"));
		addHomePageButton(navbar);
		addLoginButton(navbar);
		addLogoutButton(navbar);
		addRegisterButton(navbar);
		addRecommendationsPageButton(navbar);
		navbar.setVisible(!headless);
		return navbar;
	}

	protected OxygenCarousel createCarouselSlider() throws IOException{
		List<ICarouselImage> carouselImages = Arrays.asList(new CarouselImage(new ClassPathResource("images/slider/1.jpg", AbstractWebPage.class).getPath(), "Welcome to the <span>Repo Recommendations Project</span>", "You can get nice project recommendations just by supplying project descriptions."),
																 new CarouselImage(new ClassPathResource("images/slider/2.jpg", AbstractWebPage.class).getPath(), "<span>GitHub</span> repos", "You'll be getting GitHub Repository references."),
																 new CarouselImage(new ClassPathResource("images/slider/3.jpg", AbstractWebPage.class).getPath(), "<span>UML</span> Class Diagram", "Moreover, if the project is a Java project, you'll be provided with an UML diagram"));
		OxygenCarousel carouselSlider = new OxygenCarousel("carouselSlider", Model.ofList(carouselImages), "http://localhost:8087/licenta/login#contact");
		return carouselSlider;
	}
}
