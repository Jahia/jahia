package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.definition.ClassificationEditor;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:44:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClassificationTabItem extends EditEngineTabItem {
    private ClassificationEditor classificationEditor;

    public ClassificationTabItem(AbstractContentEngine engine) {
        super(Messages.get("ece_classification", "Classification"), engine);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabClassification());
    }

    @Override
    public void create() {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            setProcessed(true);
            classificationEditor = new ClassificationEditor(engine.getNode());
            add(classificationEditor);

        }
        layout();
    }

    public ClassificationEditor getClassificationEditor() {
        return classificationEditor;
    }
}
