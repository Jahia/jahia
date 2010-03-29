package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class LanguageSwitcherActionItem extends BaseActionItem {
    private List<String> languages;

    public LanguageSwitcherActionItem() {
    }

    private String siteKey = null;

    @Override
    public void handleNewLinkerSelection() {
        if (siteKey != null && siteKey.equals(JahiaGWTParameters.getSiteKey())) {
            return;
        }

        final Menu menu = new Menu();

        menu.removeAll();

        JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
        contentService.getSiteLanguages(new AsyncCallback<List<GWTJahiaLanguage>>() {
            public void onSuccess(List<GWTJahiaLanguage> gwtJahiaLanguages) {
                if (gwtJahiaLanguages != null && !gwtJahiaLanguages.isEmpty()) {
                    for (final GWTJahiaLanguage gwtJahiaLanguage : gwtJahiaLanguages) {
                        MenuItem item = new MenuItem(gwtJahiaLanguage.getDisplayName());
                        item.addSelectionListener(new SelectionListener<MenuEvent>() {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                ((EditLinker) linker).getMainModule().switchLanguage(gwtJahiaLanguage.getLanguage());
                            }
                        });
                        menu.add(item);
                    }
                }
            }

            public void onFailure(Throwable throwable) {
                Log.error("Unable to load avalibale mixin", throwable);
            }
        });

        setEnabled(true);

        if (isTextToolItem()) {
            Button button = (Button) getTextToolItem();
            button.setMenu(menu);
        }

        if (isMenuItem()) {
            MenuItem mi = getMenuItem();
            mi.setSubMenu(menu);
        }
        siteKey = JahiaGWTParameters.getSiteKey();
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
    }
}