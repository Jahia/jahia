package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 2:41:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateReusableContentEngine extends CreateContentEngine {
    public CreateReusableContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName) {
        super(linker, parent, type, targetName);
    }

    public CreateReusableContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, String targetName, boolean createInParentAndMoveBefore) {
        super(linker, parent, type, targetName, createInParentAndMoveBefore);
    }

    public CreateReusableContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore) {
        super(linker, parent, type, props, targetName, createInParentAndMoveBefore);
    }

    @Override
    protected void initTabs() {
        tabs.add(new ContentTabItem(this));
        tabs.add(new MetadataTabItem(this));
        tabs.add(new ClassificationTabItem(this));
        tabs.add(new RightsTabItem(this));
    }

    @Override
    protected void initFooter() {
        Button ok = new Button(Messages.getResource("fm_save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setEnabled(true);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
            ok.addSelectionListener(new CreateSelectionListener());

        buttonBar.add(ok);

        Button cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                CreateReusableContentEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }
}
