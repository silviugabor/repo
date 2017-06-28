package repo.web.wicket.bootstrap;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.core.markup.html.bootstrap.carousel.ICarouselImage;

public class OxygenCarousel extends Panel {

	private static final long serialVersionUID = 1L;
	private IModel<List<ICarouselImage>> model;
	private String buttonUrlLink;

	public OxygenCarousel(String markupId, IModel<List<ICarouselImage>> model, String buttonUrlLink) {
		super(markupId, model);
		this.model = model;
		this.buttonUrlLink = buttonUrlLink;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(newImageList("images"));
	}
	
	protected Component newImageList(String wicketId) {
		return new ListView<ICarouselImage>(wicketId, model) {

			private static final long serialVersionUID = 1L;

			@Override
            protected void populateItem(ListItem<ICarouselImage> item) {
                final ICarouselImage carouselImage = item.getModelObject();

                final Component link = newLink("buttonLink", carouselImage);
                final Component header = newHeader("header", carouselImage);
                final Component description = newDescription("description", carouselImage);
                header.setEscapeModelStrings(false);
                if (item.getIndex() == 0) {
                    item.add(new CssClassNameAppender("active"));
                }

                item.add(header, description, link);
                item.add(new AttributeModifier("style", String.format("background-image: url(%s)", carouselImage.url())));
            }
        };
	}
	
	/**
     * creates a new image element
     *
     * @param markupId the markup id of the header
     * @param image    the current image for this header
     * @return new image component
     */
    protected Component newLink(final String markupId, final ICarouselImage image) {
        final Link<String> link = new Link<String>(markupId){

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				
			}
        	
        };
        link.add(new AttributeModifier("href", buttonUrlLink));

        return link;
    }

    /**
     * creates a new image description element
     *
     * @param markupId the markup id of the header
     * @param image    the current image for this header
     * @return new description component
     */
    protected Component newDescription(final String markupId, final ICarouselImage image) {
        Label description = new Label(markupId);
        if (image.description() != null) {
            description.setDefaultModel(Model.of(image.description()));
        } else {
            description.setVisible(false);
        }

        return description;
    }

    /**
     * creates a new image header element
     *
     * @param markupId the markup id of the header
     * @param image    the current image for this header
     * @return new header component
     */
    protected Component newHeader(final String markupId, final ICarouselImage image) {
        final Label header = new Label(markupId);
        if (image.header() != null) {
            header.setDefaultModel(Model.of(image.header()));
        } else {
            header.setVisible(false);
        }

        return header;
    }
	
}
