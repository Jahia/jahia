package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:31:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePageTabItem extends PropertiesTabItem {
    private static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();

    protected List<GWTJahiaNode> reusable;
    protected ComboBox<GWTJahiaNode> combo;

    public CreatePageTabItem(AbstractContentEngine engine) {
        super("Page infos",engine, GWTJahiaItemDefinition.CONTENT);
        setMultiLang(true);
    }


    @Override
    public void postCreate() {

        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading("Template");
        fieldSet.setId("template");
        fieldSet.setCollapsible(true);
        fieldSet.setStyleAttribute("padding", "0");
        FormPanel form = new FormPanel();
        form.setFieldWidth(500);
        form.setLabelWidth(80);
        form.setHeaderVisible(false);
        form.setFrame(false);
        fieldSet.add(form);

        final ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>();

        combo = new ComboBox<GWTJahiaNode>();
        combo.setFieldLabel("Template");
        combo.setName("template");
        combo.setStore(store);
        combo.setDisplayField("displayName");
        combo.setValueField("uuid");
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
//        combo.setForceSelection(true);
        form.add(combo);

        propertiesEditor.add(fieldSet);

        definitionService.getPageTemplates(new AsyncCallback<List<GWTJahiaNode>>() {
            public void onFailure(Throwable caught) {
                Log.error("",caught);
            }

            public void onSuccess(List<GWTJahiaNode> result) {
                store.add(result);

                layout();
            }
        });


        super.postCreate();
    }

//    class VisibilityPanel extends LayoutContainer {
//        VisibilityPanel() {
//            FieldSet f = new FieldSet();
//            f.setHeading("Visibility");
//            add(f);
//        }
//    }
//
//    class TagPanel extends LayoutContainer {
//        TagPanel() {
//            FieldSet f = new FieldSet();
//            f.setHeading("Tags");
//            add(f);
//        }
//    }

    public List<GWTJahiaNode> getTemplate() {
        return combo.getSelection();
    }
}
