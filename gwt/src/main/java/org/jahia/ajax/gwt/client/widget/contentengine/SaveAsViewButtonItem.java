package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 11/5/12
 * Time: 9:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class SaveAsViewButtonItem extends SaveButtonItem {

    public Button create(final AbstractContentEngine engine) {
        Button button = new Button(Messages.get("label.saveasnewview","Save as ..."));
        button.setHeight(BUTTON_HEIGHT);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                // Open popup to select module

                final Window popup = new Window();
                popup.setHeading(Messages.get("label.saveAsView","Save as view"));
                popup.setHeight(200);
                popup.setWidth(350);
                popup.setModal(true);
                FormPanel f = new FormPanel();
                f.setHeaderVisible(false);
                final SimpleComboBox<String> dependenciesCombo = new SimpleComboBox<String>();
                if (JahiaGWTParameters.getSiteNode() != null && JahiaGWTParameters.getSiteNode().getProperties().get("j:dependencies") != null) {
                    dependenciesCombo.setStore(new ListStore<SimpleComboValue<String>>());
                    dependenciesCombo.setFieldLabel("module");
                    dependenciesCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
                    for (String s : (List<String>) JahiaGWTParameters.getSiteNode().getProperties().get("j:dependencies")) {
                        dependenciesCombo.add(s);
                    }
                    f.add(dependenciesCombo);
                }
                final TextField<String> templateType = new TextField<String>();
                templateType.setFieldLabel("template Type");
                f.add(templateType);

                final TextField<String> viewName = new TextField<String>();
                viewName.setFieldLabel("View name");
                f.add(viewName);

                Button b = new Button("submit");
                f.addButton(b);
                b.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        String[] filePath = engine.getLinker().getSelectionContext().getMainNode().getPath().split("/");
                        if (filePath.length != 6) {
                            String modulePath = "/" + filePath[1] + "/"+ filePath[2];
                            String moduleName = filePath[2];
                            String moduleVersion =filePath[3];
                            String fileName = filePath[6];
                            String fileView = fileName.substring(fileName.indexOf("."),fileName.lastIndexOf("."));
                            String fileType = filePath[4];
                            String fileTemplateType = filePath[5];
                            for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
                                if (n.getName().equals(dependenciesCombo.getSimpleValue())) {
                                    moduleName = dependenciesCombo.getSimpleValue();
                                    modulePath = n.getPath().replace("/modules/","/" + filePath[1] + "/");
                                    moduleVersion = (String) n.getProperties().get("j:versionInfo");
                                    break;
                                }
                            }
                            fileTemplateType = !"".equals(templateType.getValue())?templateType.getValue():fileTemplateType;
                            fileView = !"".equals(viewName.getValue())?viewName.getValue():fileView;

                            modulePath = modulePath + "/" +
                                    moduleVersion + "/" +
                                    fileType + "/" +
                                    fileTemplateType + "/";

                            String newViewName = fileType.split("_")[1] + "." + fileView + fileName.substring(fileName.lastIndexOf("."));
                            Map<String, String> parentNodesType = new LinkedHashMap<java.lang.String, java.lang.String>();

                            parentNodesType.put(filePath[1], "jnt:folder");
                            parentNodesType.put(moduleName, "jnt:folder");
                            parentNodesType.put(moduleVersion, "jnt:folder");
                            parentNodesType.put(fileType, "jnt:folder");
                            parentNodesType.put(fileTemplateType, "jnt:folder");
                            prepareAndSave(modulePath, newViewName, parentNodesType, engine);

                        } else {
                            MessageBox.alert("save not work as excpected","An issue occurs when trying to resolve " + engine.getLinker().getSelectionContext().getMainNode().getPath(),null);
                        }
                        popup.hide();
                    }
                });
                Button c = new Button("Cancel");
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

    protected void prepareAndSave(String modulePath,String viewName,Map<String, String> parentNodesType, final AbstractContentEngine engine) {



        JahiaContentManagementService.App.getInstance().createNode(modulePath, viewName, "jnt:viewFile", null, null, null, null, parentNodesType, new AsyncCallback<GWTJahiaNode>() {
            @Override
            public void onFailure(Throwable throwable) {
                MessageBox.alert("save not work as excpected",throwable.getMessage(),null);
            }

            @Override
            public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                engine.close();
            }
        });


    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, boolean closeAfterSave) {
        // Resolve new view path
        String modulePath;



    }
}
