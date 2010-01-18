package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.Label;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:34:40 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PropertiesTabItem extends EditEngineTabItem {
    protected PropertiesEditor propertiesEditor;
    protected String dataType;
    protected List<String> excludedTypes;

    protected PropertiesTabItem(String title, AbstractContentEngine engine, String dataType) {
        super(title, engine);
        this.dataType = dataType;
    }

    public PropertiesEditor getPropertiesEditor() {
        return propertiesEditor;
    }

    @Override
    public void create() {
        if (engine.getMixin() != null) {
            setProcessed(true);

            if (engine.isExistingNode() && engine.getNode().getNodeTypes().contains("jmix:shareable")) {
                Label label = new Label("Important : This is a shared node, editing it will modify its value for all its usages");
                label.setStyleAttribute("color", "rgb(200,80,80)");
                label.setStyleAttribute("font-size", "14px");
                add(label);
            }

            propertiesEditor = new PropertiesEditor(engine.getNodeTypes(), engine.getMixin(), engine.getProperties(), false, true, dataType, null, excludedTypes, !engine.isExistingNode() || engine.getNode().isWriteable(), true);

            postCreate();

            layout();
        }
    }

    public void postCreate() {
        add(propertiesEditor);
    }
}
