package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 27, 2010
 * Time: 10:33:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ListOrderingContentTabItem extends ContentTabItem {
    private ManualListOrderingEditor manualListOrderingEditor = null;


    public ListOrderingContentTabItem(AbstractContentEngine engine) {
        super(engine);
    }

    public ListOrderingContentTabItem(AbstractContentEngine engine, boolean multilangue) {
        super(engine, multilangue);
    }

    @Override
    public void attachPropertiesEditor() {
        if (wrapperPanel == null) {
            wrapperPanel = new LayoutContainer(new RowLayout());
            add(wrapperPanel);
        }
        wrapperPanel.add(propertiesEditor);

        attachManualListOrderingEditor(propertiesEditor);
    }

    /**
     * Create manual list ordering edirtor
     *
     * @param propertiesEditor
     * @return
     */
    private void attachManualListOrderingEditor(final PropertiesEditor propertiesEditor) {
        manualListOrderingEditor = new ManualListOrderingEditor(engine.getNode());

        // create a field set for the manual ranking
        final FieldSet fieldSet = new FieldSet();
        fieldSet.setCollapsible(true);
        fieldSet.setHeading(Messages.get("label_manualRanking", "Manual ranking"));
        final CheckBox useManualRanking = new CheckBox();
        useManualRanking.setBoxLabel(Messages.get("label_useManualRanking", "Use manual ranking"));

        useManualRanking.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent componentEvent) {
                if (useManualRanking.getValue()) {
                    propertiesEditor.getRemovedTypes().add("jmix:orderedList");
                    propertiesEditor.getAddedTypes().remove("jmix:orderedList");
                } else {
                    propertiesEditor.getRemovedTypes().remove("jmix:orderedList");
                    propertiesEditor.getAddedTypes().add("jmix:orderedList");
                }

                // update form components
                for (Component component : propertiesEditor.getOrderingListFieldSet()) {
                    if (useManualRanking.getValue()) {
                        component.setData("addedField", null);
                        component.setEnabled(false);
                    } else {
                        component.setData("addedField", "true");
                        component.setEnabled(true);
                    }
                }
                manualListOrderingEditor.setEnabled(useManualRanking.getValue());
            }
        });


        // update form components
        boolean isManual = !propertiesEditor.getNodeTypes().contains(new GWTJahiaNodeType("jmix:orderedList"));
        for (Component component : propertiesEditor.getOrderingListFieldSet()) {
            component.setEnabled(!isManual);
        }
        useManualRanking.setValue(isManual);
        manualListOrderingEditor.setEnabled(isManual);

        fieldSet.add(useManualRanking);
        fieldSet.add(manualListOrderingEditor);
        wrapperPanel.add(fieldSet);
    }

    /**
     * Get manual ordered children list
     * @return
     */
    public List<GWTJahiaNode> getNewManualOrderedChildrenList() {
        if (manualListOrderingEditor == null || !manualListOrderingEditor.isEnabled()) {
            return null;
        }
        return manualListOrderingEditor.getOrderedNodes();
    }
}
