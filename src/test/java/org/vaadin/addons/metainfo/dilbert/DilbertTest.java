package org.vaadin.addons.metainfo.dilbert;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@SuppressWarnings("serial")
@Route("")
public class DilbertTest extends VerticalLayout {

	public DilbertTest() {
		String username = "testuser";
		Dilbert dilbert = new Dilbert(username);
		
		Checkbox force = new Checkbox("Force (not only once a day per user)", false);

		for(DilbertType type : DilbertType.values()) {
			Button button = new Button(type.getText());
			button.addClickListener(click -> dilbert.show(type, force.getValue()));
			add(button);
		}

		add(force);
	}
}
