package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * toolbar item that display warnings if available
 * Created by david on 21/05/14.
 */
public class DisplayToolbarWarningsActionItem extends BaseActionItem {
    private static final long serialVersionUID = 3328698500846922180L;
    private transient Button b;
    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        b = new Button();
    }

    @Override
    public Component createNewToolItem() {
        String messages = JahiaGWTParameters.getParam(JahiaGWTParameters.TOOLBAR_MESSAGES);
        if (messages != null &&  messages.length() > 0) {
            b.setEnabled(false);
            b.setText(Messages.get("label.notifications","Notifications"));
            String[] messagesTab = messages.split("\\|\\|");
            final Menu menu = new Menu();
            b.setMenu(menu);
            for (String s : messagesTab) {
                MenuItem m = new MenuItem();
                m.setText(s);
                menu.add(m);
            }
            b.setEnabled(true);
        } else {
            b.hide();
        }
        return b;
    }


}
