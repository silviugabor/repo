package repo.web.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

public class RequiredBehavior extends Behavior {

	private static final long serialVersionUID = 1L;

	@Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        tag.put("required", "required");
    }
}