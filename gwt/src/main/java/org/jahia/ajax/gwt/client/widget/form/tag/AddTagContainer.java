package org.jahia.ajax.gwt.client.widget.form.tag;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;


/**
 * Created by kevan on 02/07/14.
 */
public class AddTagContainer extends HorizontalPanel {
    protected TagComboBox tagComboBox;
    protected Button addTagButton;
    protected TagField tagField;

    public AddTagContainer(TagField _tagField, boolean autoComplete, String separator) {
        super();
        tagField = _tagField;
        tagComboBox = new TagComboBox(autoComplete);
        addTagButton = new Button(Messages.get("label.add"));
        addTagButton.setIcon(StandardIconsProvider.STANDARD_ICONS.plusRound());
        addTagButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                String tag = null;
                if(tagComboBox.getValue() != null){
                    tag = tagComboBox.getValue().getValue();
                } else if(tagComboBox.getRawValue() != null && tagComboBox.getRawValue().trim().length() > 0){
                    tag = tagComboBox.getRawValue().trim();
                }
                if(tag != null) {
                    tagField.addTag(tag);
                    tagComboBox.setRawValue("");
                }
            }
        });

        add(tagComboBox);
        add(addTagButton);
    }
}