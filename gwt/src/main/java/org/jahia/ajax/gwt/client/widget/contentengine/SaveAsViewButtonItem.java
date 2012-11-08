package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SiteSwitcherActionItem;

import java.util.*;


/**
 * User: david
 * Date: 11/5/12
 * Time: 9:59 AM
 * Defines a button that opens a popup and allows to save a view ( see ModuleDataSource ) with custom parameters
 */
public class SaveAsViewButtonItem extends SaveButtonItem {

    public Button create(final AbstractContentEngine engine) {
        Button button = new Button(Messages.get("label.saveAsNewView", "Save as ..."));
        button.setHeight(BUTTON_HEIGHT);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                final String[] filePath = engine.getLinker().getSelectionContext().getMainNode().getPath().split("/");
                if (filePath.length != 7) {
                    MessageBox.alert("save not work as excpected", "An issue occurs when trying to resolve " + engine.getLinker().getSelectionContext().getMainNode().getPath(), null);
                }
                final String modulePath = "/" + filePath[1] + "/" + filePath[2];
                final String moduleName = filePath[2];
                final String moduleVersion = filePath[3];
                final String fileName = filePath[6];
                final String fileView = fileName.indexOf(".") == fileName.lastIndexOf(".") ? "" : fileName.substring(fileName.indexOf(".") + 1, fileName.lastIndexOf("."));
                final String fileType = filePath[4];
                final String fileTemplateType = filePath[5];


                // Open popup to select module

                final Window popup = new Window();
                popup.setHeading(Messages.get("label.saveAsView", "Save as view"));
                popup.setHeight(200);
                popup.setWidth(350);
                popup.setModal(true);
                FormPanel f = new FormPanel();
                f.setHeaderVisible(false);
                final SimpleComboBox<String> dependenciesCombo = new SimpleComboBox<String>();
                if (JahiaGWTParameters.getSiteNode() != null && JahiaGWTParameters.getSiteNode().getProperties().get("j:dependencies") != null) {
                    dependenciesCombo.setStore(new ListStore<SimpleComboValue<String>>());
                    dependenciesCombo.setFieldLabel(Messages.get("label.module", "module"));
                    dependenciesCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
                    dependenciesCombo.add(moduleName);
                    for (String s : (List<String>) JahiaGWTParameters.getSiteNode().getProperties().get("j:dependencies")) {
                        dependenciesCombo.add(s);
                    }
                    dependenciesCombo.setSimpleValue(moduleName);
                    f.add(dependenciesCombo);
                }
                final TextField<String> templateType = new TextField<String>();
                templateType.setFieldLabel(Messages.get("label.templateType", "template Type"));
                templateType.setValue(fileTemplateType);
                f.add(templateType);

                final TextField<String> viewName = new TextField<String>();
                viewName.setFieldLabel(Messages.get("label.viewName", "View name"));
                viewName.setValue(fileView);
                f.add(viewName);

                Button b = new Button(Messages.get("label.submit", "submit"));
                f.addButton(b);
                b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        String newModuleName = moduleName;
                        String newModulePath = modulePath;
                        String newModuleVersion = moduleVersion;
                        GWTJahiaNode newModuleNode = null;
                        if (!newModuleName.equals(dependenciesCombo.getSimpleValue())) {
                            for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                                if (n.getName().equals(dependenciesCombo.getSimpleValue())) {
                                    newModuleNode = n;
                                    newModuleName = dependenciesCombo.getSimpleValue();
                                    newModulePath = n.getPath().replace("/modules/", "/" + filePath[1] + "/");
                                    newModuleVersion = (String) n.getProperties().get("j:versionInfo");
                                    break;
                                }
                            }
                        }
                        String newfileTemplateType = !"".equals(templateType.getValue()) ? templateType.getValue() : fileTemplateType;
                        String newfileView = viewName.isDirty() ? viewName.getValue() != null? "." + viewName.getValue():"" : "." + fileView;
                        newModulePath = newModulePath + "/" +
                                newModuleVersion + "/" +
                                fileType + "/" +
                                newfileTemplateType + "/";

                        String newViewName = fileType.split("_")[1] + newfileView + fileName.substring(fileName.lastIndexOf("."));
                        Map<String, String> parentNodesType = new LinkedHashMap<java.lang.String, java.lang.String>();

                        parentNodesType.put(filePath[1], "jnt:folder");
                        parentNodesType.put(newModuleName, "jnt:folder");
                        parentNodesType.put(newModuleVersion, "jnt:folder");
                        parentNodesType.put(fileType, "jnt:folder");
                        parentNodesType.put(newfileTemplateType, "jnt:folder");
                        prepareAndSave(newModulePath, newViewName, parentNodesType, engine, newModuleNode);
                        popup.hide();
                    }
                });
                Button c = new Button(Messages.get("label.cancel", "Cancel"));
                c.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        popup.hide();
                    }
                });
                f.addButton(c);
                f.setButtonAlign(Style.HorizontalAlignment.CENTER);

                FormButtonBinding binding = new FormButtonBinding(f);
                binding.addButton(b);
                popup.add(f);
                popup.show();
            }
        });
        return button;
    }

    protected void prepareAndSave(final String modulePath, String viewName, Map<String, String> parentNodesType, final AbstractContentEngine engine, final GWTJahiaNode newModuleNode) {

        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();
        properties.addAll(engine.getProperties().values());
        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            item.doSave(null, engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, removedTypes, engine.getAcl());
        }
        for (GWTJahiaNodeProperty p : properties) {
            for (GWTJahiaNodeProperty p1 : engine.getChangedProperties()) {
                if (p.getName().equals(p1.getName())) {
                    properties.get(properties.indexOf(p)).setValues(p1.getValues());
                }
            }
        }
        JahiaContentManagementService.App.getInstance().createNode(modulePath, viewName, "jnt:viewFile", null, engine.getAcl(), properties, engine.changedI18NProperties, parentNodesType, new AsyncCallback<GWTJahiaNode>() {
            public void onFailure(Throwable throwable) {
                MessageBox.alert("save not work as excpected", throwable.getMessage(), null);
            }
            public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                Linker linker = engine.getLinker();
                engine.close();
                if (newModuleNode == null) {
                    linker.refresh(Linker.REFRESH_SOURCES);
                } else {
                    JahiaGWTParameters.setSite(gwtJahiaNode, linker);
                    linker.refresh(Linker.REFRESH_ALL);
                    MainModule.staticGoTo(gwtJahiaNode.getPath(), null);
                    SiteSwitcherActionItem.refreshAllSitesList(linker);
                }
            }
        });


    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, boolean closeAfterSave) {
    }
}
