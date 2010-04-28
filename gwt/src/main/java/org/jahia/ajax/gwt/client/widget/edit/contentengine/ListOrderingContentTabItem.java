package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
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
 * Edit engine tab for performing actions on the list of items:
 * manual and automatic ordering, deletion etc.
 * User: ktlili
 * Date: Apr 27, 2010
 * Time: 10:33:26 AM
 */
public class ListOrderingContentTabItem extends ContentTabItem {
    private ManualListOrderingEditor manualListOrderingEditor = null;
    private static final String JMIX_ORDERED_LIST = "jmix:orderedList";


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
     * Create manual list ordering editor
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
                    propertiesEditor.getRemovedTypes().add(JMIX_ORDERED_LIST);
                    propertiesEditor.getAddedTypes().remove(JMIX_ORDERED_LIST);
                } else {
                    propertiesEditor.getRemovedTypes().remove(JMIX_ORDERED_LIST);
                    propertiesEditor.getAddedTypes().add(JMIX_ORDERED_LIST);
                }

                // update form components
                for (FieldSet component : propertiesEditor.getOrderingListFieldSet()) {
                    if (useManualRanking.getValue()) {
                        component.setData("addedField", null);
                        component.setEnabled(false);
                        component.collapse();
                    } else {
                        component.setData("addedField", "true");
                        component.setEnabled(true);
                        component.expand();
                    }
                }
                manualListOrderingEditor.setEnabled(useManualRanking.getValue());
                if (useManualRanking.getValue()) {
                    manualListOrderingEditor.expand();
                } else {
                    manualListOrderingEditor.collapse();
                }
            }
        });


        // update form components
        boolean isManual = !propertiesEditor.getNodeTypes().contains(new GWTJahiaNodeType(JMIX_ORDERED_LIST));
        for (FieldSet component : propertiesEditor.getOrderingListFieldSet()) {
            component.setEnabled(!isManual);
            if (isManual) {
                component.collapse();
            }
        }
        useManualRanking.setValue(isManual);
        manualListOrderingEditor.setEnabled(isManual);
        if (!isManual) {
            manualListOrderingEditor.collapse();
        }

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
