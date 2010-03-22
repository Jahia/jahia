package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:31:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatePageTabItem extends EditEngineTabItem {
    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();

    protected GWTJahiaNodeType type;
    protected List<GWTJahiaNode> reusable;
    protected ComboBox<GWTJahiaNode> combo;
    protected Field titleField;

    public CreatePageTabItem(AbstractContentEngine engine) {
        super("Page infos",engine);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {

        setProcessed(true);

        BorderLayout l = new BorderLayout();
        setLayout(l);

        definitionService.getNodeTypeWithReusableComponents("jnt:page", new AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNode>>>() {
            public void onFailure(Throwable caught) {
                Log.error("",caught);
            }

            public void onSuccess(Map<GWTJahiaNodeType, List<GWTJahiaNode>> result) {
                type = result.keySet().iterator().next();
                reusable = result.get(type);

                FormPanel form = new FormPanel();

                form.setPadding(5);
                form.setFieldWidth(500);
                form.setLabelWidth(80);
                form.setCollapsible(false);
                form.setFrame(false);
                form.setAnimCollapse(false);
                form.setBorders(false);
                form.setBodyBorder(false);
                form.setHeaderVisible(false);
                form.setScrollMode(Style.Scroll.AUTO);
                form.setButtonAlign(Style.HorizontalAlignment.CENTER);

                FieldSet set = new FieldSet();
                FormPanel formset = new FormPanel();
                formset.setFieldWidth(500);
                formset.setLabelWidth(80);
                formset.setHeaderVisible(false);
                formset.setFrame(false);
                set.add(formset);

                List<GWTJahiaItemDefinition> items = type.getInheritedItems();
                for (GWTJahiaItemDefinition item : items) {
                    if (item.getName().equals("jcr:title")) {
                        titleField = FormFieldCreator.createField(item, null);
                        set.setHeading(item.getDeclaringNodeTypeLabel());
                        formset.add(titleField);
                        break;
                    }
                }

                form.add(set);

                set = new FieldSet();
                set.setHeading("Schmurtz");
                formset = new FormPanel();
                formset.setFieldWidth(500);
                formset.setLabelWidth(80);
                formset.setHeaderVisible(false);
                formset.setFrame(false);
                set.add(formset);

                ListStore<GWTJahiaNode> store = new ListStore<GWTJahiaNode>();
                store.add(reusable);
                combo = new ComboBox<GWTJahiaNode>();
                combo.setFieldLabel("schmurtz");
                combo.setStore(store);
                combo.setDisplayField("displayName");
                combo.setValueField("uuid");
                combo.setTypeAhead(true);
                combo.setTriggerAction(ComboBox.TriggerAction.ALL);
//                combo.setForceSelection(true);
                formset.add(combo);

                form.add(set);

                BorderLayoutData layoutData = new BorderLayoutData(Style.LayoutRegion.CENTER);
                add(form, layoutData);
                layout();
            }
        });


        BorderLayoutData layoutData = new BorderLayoutData(Style.LayoutRegion.EAST, 200);
        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.add(new VisibilityPanel());
        layoutContainer.add(new TagPanel());
        add(layoutContainer, layoutData);

    }

    class VisibilityPanel extends LayoutContainer {
        VisibilityPanel() {
            FieldSet f = new FieldSet();
            f.setHeading("Visibility");
            add(f);
        }
    }
    
    class TagPanel extends LayoutContainer {
        TagPanel() {
            FieldSet f = new FieldSet();
            f.setHeading("Tags");
            add(f);
        }
    }

    public String getContentTitle() {
        return (titleField.getValue() != null)?titleField.getValue().toString():"";
    }

    public List<GWTJahiaNode> getReusableComponent() {
        return combo.getSelection();
    }
}
