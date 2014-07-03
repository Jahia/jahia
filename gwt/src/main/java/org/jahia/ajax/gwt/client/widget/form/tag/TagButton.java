package org.jahia.ajax.gwt.client.widget.form.tag;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

/**
 * Created by kevan on 02/07/14.
 */
public class TagButton extends HorizontalPanel {
    private Field<String> tagField;
    private Button tagButton;

    public TagButton(String tag) {
        super();
        this.setBorders(true);
        this.setSpacing(2);
        this.setStyleAttribute("marginRight", "5px");
        this.setStyleAttribute("marginBottom", "5px");
        this.setStyleAttribute("backgroundColor", "white");
        tagButton = new Button();
        tagButton.setIcon(StandardIconsProvider.STANDARD_ICONS.delete());
        Text tagText = new Text(tag);
        tagText.setStyleAttribute("margin", "4px");
        add(tagText);
        add(tagButton);
        this.tagField = new TextField<String>();
        this.tagField.setValue(tag);
    }

    public Field<String> getTagField() {
        return tagField;
    }

    public Button getTagButton() {
        return tagButton;
    }
}