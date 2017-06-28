package repo.web.wicket.page;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.IResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantumldependency.commoncli.exception.CommandLineException;
import repo.api.service.RepositoryService;
import repo.crawler.entities.github.GitHubProgrammingLanguageType;
import repo.crawler.entities.github.GitHubRepository;
import repo.uml.exception.DiagramGenerationException;
import repo.uml.service.DiagramGenerationService;
import repo.web.application.security.spring.Roles;
import repo.web.wicket.ByteArrayResourceStream;
import repo.web.wicket.HeaderFooter;
import repo.web.wicket.ajax.AjaxDownloadBehavior;
import repo.web.wicket.behavior.PlaceholderBehavior;

@MountPath("/recommend")
@AuthorizeInstantiation({Roles.USER})
public class RecommendationsPage extends HeaderFooter{

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(RecommendationsPage.class);
	private WebMarkupContainer repositoryContainer;
	
	@SpringBean
	private RepositoryService repositoryService;
	
	@SpringBean
	private DiagramGenerationService diagramGenerationService;
	
	public RecommendationsPage() {
		super();
	}

	public RecommendationsPage(PageParameters parameters) {
		super(parameters);
	}
	
	private Label newRepositorySimilarityScore(String markupId, GitHubRepository gitHubRepository){
		return new Label(markupId, Model.of(gitHubRepository.getSimilarityScore()));
	}
	
	private Label newRepositoryName(String markupId, GitHubRepository gitHubRepository){
		return new Label(markupId, Model.of(gitHubRepository.getRepositoryName()));
	}
	
	private Label newRepositoryDescription(String markupId, GitHubRepository gitHubRepository){
		return new Label(markupId, Model.of(new String(gitHubRepository.getDescription())));
	}
	
	private Label newRepositoryProgrammingLanguages(String markupId, GitHubRepository gitHubRepository){
		List<GitHubProgrammingLanguageType> gitHubProgrammingLanguageTypes = gitHubRepository.getProgrammingLanguageTypeList();
		String programmingLanguages = new String();
		if(gitHubProgrammingLanguageTypes != null && gitHubProgrammingLanguageTypes.size() > 0){
			StringBuilder sb = new StringBuilder("");
			gitHubProgrammingLanguageTypes.forEach((gitHubProgrammingLanguage)->{
				sb.append(gitHubProgrammingLanguage).append(", ");
			});
			programmingLanguages = sb.substring(0, sb.length() - 2);
		}
		return new Label(markupId, Model.of(programmingLanguages));
	}
	
	private ExternalLink newRepositoryLink (String markupId, GitHubRepository gitHubRepository){
		return new ExternalLink(markupId, gitHubRepository.getRepositoryLink(), "View");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(createPageTitleTag("recommendations.title"));
		setDefaultModel(Model.ofList(Collections.EMPTY_LIST));
		GitHubRepository gitHubRepository = new GitHubRepository();
		
		oxygenCarousel.setVisible(false);
		repositoryContainer = new WebMarkupContainer("recommendationsTable");
		ListView<GitHubRepository> listView = new ListView<GitHubRepository>("repositoryRow", (IModel<? extends List<GitHubRepository>>) this.getDefaultModel()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<GitHubRepository> item) {
				final GitHubRepository gitHubRepository = item.getModelObject();
				
				final AjaxDownloadBehavior download = new AjaxDownloadBehavior() {
					
					private static final long serialVersionUID = 1L;

					@Override
					protected String getFileName() {
						return "diagram.svg";
					}
					
					@Override
					protected IResourceStream getResourceStream() {
						byte[] diagramByteContent;
						try {
							diagramByteContent = diagramGenerationService.generateClassDiagram(gitHubRepository.getId(), FileFormat.SVG);
							return new ByteArrayResourceStream(diagramByteContent);
						} catch (DiagramGenerationException | IOException | CommandLineException | ParseException e) {
							LOG.error("Could not download diagram attachement for repository with ID " + gitHubRepository.getId(), e);
							return null;
						}
					}
				};
				
				final Component repositoryIndex = new Label("repositoryIndex", Model.of(item.getIndex() + 1));
				final Component repositoryName = newRepositoryName("repositoryName", gitHubRepository);
				final Component repositoryDescription = newRepositoryDescription("repositoryDescription", gitHubRepository);
				final Component repositoryProgrammingLanguages = newRepositoryProgrammingLanguages("programmingLanguages", gitHubRepository);
				final Component repositorySimilarityScore = newRepositorySimilarityScore("similarityScore", gitHubRepository);
				
				final Component viewRepositoryLink = newRepositoryLink("viewRepositoryLink", gitHubRepository);
				final Component downloadDiagramLink = new AjaxLink<Void>("downloadDiagramLink") {

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						download.initiate(target);
					}
				};
				downloadDiagramLink.add(download);
				downloadDiagramLink.setOutputMarkupPlaceholderTag(true);
				downloadDiagramLink.setVisible(gitHubRepository.getProgrammingLanguageTypeList().contains(GitHubProgrammingLanguageType.JAVA));
				item.add(repositoryIndex, repositoryName, repositoryDescription, repositoryProgrammingLanguages, repositorySimilarityScore, viewRepositoryLink, downloadDiagramLink);
				
			}
		};
		repositoryContainer.add(listView);
		repositoryContainer.setOutputMarkupPlaceholderTag(true);
		repositoryContainer.setVisible(((List<?>)this.getDefaultModelObject()).size() != 0);
		
		Form<Void> recommendationForm = new Form<>("recommendationForm");
		TextField<String> descriptionTextField = new TextField<>("repositoryRecommendationDescription", new Model<String>(){

			private static final long serialVersionUID = 1L;
			
			@Override
			public String getObject() {
				return gitHubRepository.getDescription() == null ? new String() : new String(gitHubRepository.getDescription());
			}
			@Override
			public void setObject(String object) {
				gitHubRepository.setDescription(object.getBytes());
			}
			
		});
		descriptionTextField.add(new PlaceholderBehavior("Repository Description"));
		
		IndicatingAjaxButton submitLink = new IndicatingAjaxButton("submit", recommendationForm) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				List<GitHubRepository> recommendedRepositories = repositoryService.getRecommmendations(2222L, new String(gitHubRepository.getDescription()), 20);
				RecommendationsPage.this.setDefaultModelObject(recommendedRepositories);
				repositoryContainer.setVisible(true);
				target.add(repositoryContainer);
			}
		};
		recommendationForm.add(descriptionTextField, submitLink);
	
		add(repositoryContainer, recommendationForm);
	}
}
