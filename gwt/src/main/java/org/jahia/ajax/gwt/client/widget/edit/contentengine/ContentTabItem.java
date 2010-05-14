package org.jahia.ajax.gwt.client.widget.edit.contentengine;


import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 8:10:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContentTabItem extends PropertiesTabItem {
    private boolean isNodeNameFieldDisplayed = false;
    private TextField<String> name = new TextField<String>();
    protected LayoutContainer wrapperPanel;

    public TextField<String> getName() {
        return name;
    }

    public ContentTabItem(NodeHolder engine) {
        super(Messages.get("ece_content", "Content"), engine, GWTJahiaItemDefinition.CONTENT);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabContent());
        setMultiLang(true);
    }

    public ContentTabItem(NodeHolder engine, boolean multilangue) {
        this(engine);
        setMultiLang(multilangue);
    }

    @Override
    public void attachPropertiesEditor() {
        // handle jcr:title property
        if (!propertiesEditor.getFieldsMap().containsKey("jcr:title")) {
            if (wrapperPanel == null) {
                wrapperPanel = new LayoutContainer(new RowLayout());
                wrapperPanel.setScrollMode(Style.Scroll.AUTO);
                add(wrapperPanel);
            }
            FieldSet fSet = new FieldSet();
            fSet.add(createNamePanel());
            isNodeNameFieldDisplayed = true;
            propertiesEditor.insert(fSet,0);
            wrapperPanel.add(propertiesEditor);
        }

        // attach properties node
        if (wrapperPanel == null) {
            super.attachPropertiesEditor();
        }
    }


    /**
     * Get Form panel that contains the name of the nodes
     *
     * @return
     */
    private FormPanel createNamePanel() {

        FormPanel formPanel = new FormPanel();
        formPanel.setLabelAlign(FormPanel.LabelAlign.TOP);
        formPanel.setFieldWidth(550);
        formPanel.setLabelWidth(180);
        formPanel.setFrame(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setHeaderVisible(false);
        name.setFieldLabel("Name");
        name.setName("name");
        if (engine.isExistingNode()) {
            name.setValue(engine.getNode().getName());
            setData("NodeName", engine.getNode().getName());
        } else {
            name.setValue("Automatically Created (you can type your name here if you want)");
        }
        formPanel.add(name);
        return formPanel;
    }

    /**
     * Return true if nodeNameField is displayed
     *
     * @return
     */
    public boolean isNodeNameFieldDisplayed() {
        return isNodeNameFieldDisplayed;
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            wrapperPanel = null;
        }
        super.setProcessed(processed);
    }
}
