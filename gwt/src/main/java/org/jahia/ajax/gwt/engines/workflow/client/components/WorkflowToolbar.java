/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.workflow.client.components;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.commons.client.beans.GWTJahiaLabel;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaService;
import org.jahia.ajax.gwt.commons.client.rpc.JahiaServiceAsync;
import org.jahia.ajax.gwt.commons.client.ui.ReportGrid;
import org.jahia.ajax.gwt.commons.client.ui.WorkflowBatchViewer;
import org.jahia.ajax.gwt.commons.client.ui.language.LanguageSelectedListener;
import org.jahia.ajax.gwt.commons.client.ui.language.LanguageSwitcher;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.engines.workflow.client.WorkflowManager;
import org.jahia.ajax.gwt.engines.workflow.client.WorkflowService;
import org.jahia.ajax.gwt.engines.workflow.client.WorkflowServiceAsync;
import org.jahia.ajax.gwt.engines.workflow.client.model.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.engines.workflow.client.model.GWTJahiaWorkflowManagerState;
import org.jahia.ajax.gwt.tripanelbrowser.client.components.TopBar;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Toolbar for the workflow manager.
 *
 * @author rfelden
 * @version 29 juil. 2008 - 17:16:58
 */
public class WorkflowToolbar extends TopBar {

    private final WorkflowServiceAsync service = WorkflowService.App.getInstance() ;

    private Map<String, Map<String, Set<String>>> batch = new HashMap<String, Map<String, Set<String>>>() ;
    private Map<String,String> actionsLabel;
    private TextToolItem chooseAction ;
    private Menu actionsMenu ;

    private ToolBar m_component ;

    private boolean actionsInitialized = false ;

    private String action = "";
    private LanguageSwitcher languageSwitcher;

    public WorkflowToolbar() {
        m_component = new ToolBar() ;

        chooseAction = new TextToolItem(WorkflowManager.getResource("wf_chooseAction")) ;
        chooseAction.setIconStyle("wf-action");
        actionsMenu = new Menu() ;
        chooseAction.setMenu(actionsMenu);
        m_component.add(chooseAction) ;

         // this allows to align remaining items right
        m_component.add(new FillToolItem()) ;

        TextToolItem addToBatch = new TextToolItem(WorkflowManager.getResource("wf_addToBatch"));
        addToBatch.setIconStyle("wf-add");
        addToBatch.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                if (action.length() > 0) {
                    Map<String, Set<String>> checked = ((WorkflowTable) getLinker().getTopRightObject()).getChecked() ;
                    if (checked.size() > 0) {
                        addToBatch();
                    } else {
                        Window.alert(WorkflowManager.getResource("wf_nothingChecked"));
                    }
                } else {
                    Window.alert(WorkflowManager.getResource("wf_noAction")) ;
                }
            }
        });
        m_component.add(addToBatch) ;
        languageSwitcher = new LanguageSwitcher(true, true, false,false, JahiaGWTParameters.getLanguage(), false, new LanguageSelectedListener() {
            private final JahiaServiceAsync jahiaServiceAsync = JahiaService.App.getInstance();
            public void onLanguageSelected (String languageSelected) {                
                jahiaServiceAsync.changeLocaleForAllPagesAndEngines(languageSelected,new AsyncCallback() {
                    public void onFailure (Throwable throwable) {
                        //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public void onSuccess (Object o) { // TODO chained rpc calls are ugly
                        service.saveWorkflowManagerState(new GWTJahiaWorkflowManagerState(((WorkflowTable) getLinker().getTopRightObject()).getChecked(),
                                                                                          ((WorkflowTable) getLinker().getTopRightObject()).getDisabledChecks(),
                                                                                          ((WorkflowTable) getLinker().getTopRightObject()).getTitleForObjectKey(),
                                                                                          batch),
                                                         new AsyncCallback() {
                                                             public void onFailure(Throwable throwable) {
                                                                 Log.error(throwable.toString()) ;
                                                             }
                                                             public void onSuccess(Object o) {
                                                                 GWTJahiaWorkflowElement selectedPage = (GWTJahiaWorkflowElement) getLinker().getTreeSelection() ;
                                                                 if (selectedPage == null) {
                                                                     Window.Location.reload();
                                                                 } else {
                                                                     String rawUrl = Window.Location.getHref().replace(Window.Location.getQueryString(), "") ;
                                                                     Window.Location.replace(rawUrl + "?startpage=" + selectedPage.getObjectKey());
                                                                 }
                                                             }
                                                         });
                    }
                });
            }

            public void onWorkflowSelected (String text) {}
        });
        m_component.add(new AdapterToolItem(languageSwitcher));
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelection) {
        // nothing here
    }

    public Component getComponent() {
        return m_component ;
    }

    public String getAction() {
        return action;
    }

    public void showBatchReport() {
        new WorkflowBatchViewer(batch, ((WorkflowTable) getLinker().getTopRightObject()).getTitleForObjectKey(), false){
            public void buildContextMenu(final Grid<ReportGrid.GWTReportElement> grid) {
                Menu contextMenu = new Menu() ;
                final MenuItem removeAction = new MenuItem(WorkflowManager.getResource("wf_removeAction"), new SelectionListener<ComponentEvent>() {
                    public void componentSelected(ComponentEvent event) {
                        ReportGrid.GWTReportElement elem = grid.getSelectionModel().getSelectedItem() ;
                        if (elem != null) {
                            String action = elem.getAction() ;
                            String key = elem.getKey() ;
                            String lang = elem.getLanguage() ;
                            if (((WorkflowToolbar)getLinker().getTopObject()).removeAction(action, key, lang)) {
                                grid.getStore().remove(elem);
                            }

                        }
                    }
                });
                removeAction.setIconStyle("wf-remove");
                contextMenu.add(removeAction) ;
                contextMenu.addListener(Events.BeforeShow, new Listener() {
                    public void handleEvent(BaseEvent baseEvent) {
                        removeAction.setEnabled(grid.getSelectionModel().getSelectedItem() != null) ;
                    }
                }) ;
                grid.setContextMenu(contextMenu);
            }
            public void execute() {
                ((WorkflowStatusBar)getLinker().getBottomObject()).showExecuteWindow();
            }
        }.show();
    }

    public void restoreBatch(Map<String, Map<String, Set<String>>> oldBatch) {
        batch.putAll(oldBatch);
    }

    public void addToBatch() {
        Map<String, Set<String>> langsPerKey = ((WorkflowTable) getLinker().getTopRightObject()).getChecked() ;
        if (!batch.containsKey(action)) {
            batch.put(action, new HashMap<String, Set<String>>()) ;
        }
        for (String key: langsPerKey.keySet()) {
            if (!batch.get(action).containsKey(key)) {
                batch.get(action).put(key, new HashSet<String>()) ;
            }
            for (String lang: langsPerKey.get(key)) {
                batch.get(action).get(key).add(lang) ;
            }
        }
        ((WorkflowTable) getLinker().getTopRightObject()).clearChecked() ;
    }

    public Map<String, Map<String, Set<String>>> getBatch() {
        return batch ;
    }

    /**
     * Here we only need to take care of the previously checked list, the batch list will be updated as well since
     * the maps it contains are the same, only the highest level map is duplicated (<action, Map<objectKey, Set<language> >)
     *
     * @param action the action to cancel
     * @param key the key for which to cancel action
     * @param language the language to be cancelled
     * @return true if cancel went well, false otherwise
     */
    public boolean removeAction(final String action, final String key, final String language) {
        boolean result = false ;
        if (batch.containsKey(action)) {
            if (batch.get(action).containsKey(key)) {
                if (batch.get(action).get(key).contains(language)) {
                    result = batch.get(action).get(key).remove(language) ;
                }
            }
        }
        ((WorkflowTable) getLinker().getTopRightObject()).processItems();
        return result ;
    }

    public void setAvailableAction(final Collection<String> actions) {
        if (!actionsInitialized) {
            // init available actions
            service.getAvailableActions(new AsyncCallback<List<GWTJahiaLabel>>() {
                public void onFailure(Throwable throwable) {
                    // ...
                }

                public void onSuccess(List<GWTJahiaLabel> actionsList) {
                    actionsInitialized = true ;
                    actionsLabel = new HashMap<String,String>();
                    for (GWTJahiaLabel action: actionsList) {
                        actionsLabel.put(action.getKey(), action.getLabel());
                        CheckMenuItem actionMenuItem = new CheckMenuItem(action.getLabel()) ;
                        actionMenuItem.setGroup("step");
                        actionMenuItem.addSelectionListener(new ActionSelectionListener<ComponentEvent>(action.getKey(), action.getLabel()));
                        actionsMenu.add(actionMenuItem);
                    }
                    setAvailableActions(actions);
                }
            }) ;
        } else {
            setAvailableActions(actions);
        }
    }

    private void setAvailableActions(Collection<String> availableActions) {
        List<Item> l = actionsMenu.getItems();

        Set<String> s = new HashSet<String>();
        for (String availableAction : availableActions) {
            s.add(actionsLabel.get(availableAction));
        }
        CheckMenuItem enabledItem = null;
        for (Item item : l) {
            if (item instanceof CheckMenuItem) {
                if (!s.contains(((CheckMenuItem)item).getText())) {
                    Log.debug("disable " + ((CheckMenuItem)item).getText()) ;
                    item.setEnabled(false);
                } else {
                    Log.debug("enable " + ((CheckMenuItem)item).getText()) ;
                    item.setEnabled(true) ;
                    enabledItem = (CheckMenuItem) item;
                }
            }
        }
        if (enabledItem != null) {
            enabledItem.setChecked(true);
            enabledItem.fireEvent(Events.Select) ;
        } else {
            chooseAction.setText(WorkflowManager.getResource("wf_chooseAction"));
            chooseAction.setIconStyle("wf-action");
        }
    }


    public void cycleAction(Set<String> actions) {
        List<Item> l = actionsMenu.getItems();

        Set<String> s = new HashSet<String>();
        for (String availableAction : actions) {
            s.add(actionsLabel.get(availableAction));
        }

        boolean found = false;
        boolean set = false;
        for (Item item : l) {
            if (item instanceof CheckMenuItem) {
                CheckMenuItem checkMenuItem = (CheckMenuItem) item;
                if (checkMenuItem.isChecked()) {
                    if (s.contains(checkMenuItem.getText())) {
                        found = true;
                    }
                    checkMenuItem.setChecked(false);
                } else if (found) {
                    if (s.contains(checkMenuItem.getText())) {
                        checkMenuItem.setChecked(true);
                        checkMenuItem.fireEvent(Events.Select) ;
                        set = true;
                        break;
                    }
                }
            }
        }
        if (!set) {
            for (Item item : l) {
                if (item instanceof CheckMenuItem) {
                    CheckMenuItem checkMenuItem = (CheckMenuItem) item;
                    if (s.contains(checkMenuItem.getText())) {
                        checkMenuItem.setChecked(true);
                        checkMenuItem.fireEvent(Events.Select) ;
                        break;
                    }
                }
            }

        }
    }

    private class ActionSelectionListener<E extends ComponentEvent> extends SelectionListener<E> {
        private String actionName;
        private String actionLabel;

        private ActionSelectionListener(String actionName, String actionLabel) {
            this.actionName = actionName;
            this.actionLabel = actionLabel ;
        }

        public void componentSelected(E e) {
            action = actionName;
            chooseAction.setText(actionLabel);
            chooseAction.setIconStyle("wf-action-" + actionName);
            ((WorkflowTable) getLinker().getTopRightObject()).processItems();
        }
    }
}
