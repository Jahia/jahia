package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 30, 2010
 * Time: 8:16:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkInProgressActionItem extends BaseActionItem {

    private static WorkInProgressActionItem instance;

    private static List<String> statuses = new ArrayList<String>();

    @Override public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        instance = this;
    }

    public static void removeStatus(String status) {
        statuses.remove(status);
        refreshStatus();
    }

    public static void setStatus(String status) {
        statuses.add(status);
        refreshStatus();
    }

    private static void refreshStatus() {
        Button b = (Button) instance.getTextToolItem();
        if (statuses.isEmpty()) {
            b.setText(null);
            b.setIconStyle(null);
            b.setEnabled(false);
        } else if (statuses.size() == 1) {
            b.setIconStyle("x-status-busy");
            b.setText(statuses.get(0));
            b.setEnabled(true);
        } else {
            b.setIconStyle("x-status-busy");
            b.setText(statuses.size() + " tasks running ...");
            b.setEnabled(true);
        }
    }

    @Override public Component createNewToolItem() {
        Button b = new Button();
        b.setEnabled(false);
        return b;
    }
}
