package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 8, 2010
 * Time: 2:09:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwitchModeActionItem extends BaseActionItem {
    public static final int LIVE = 0;
    public static final int PREVIEW = 1;
    public static final int EDIT = 2;

    @Override
    public void onComponentSelection() {
        final String mode = getPropertyValue(getGwtToolbarItem(), "mode");
        int modeAsInt = EDIT;
        if (mode != null) {
            if (mode.equalsIgnoreCase("live")) {
                modeAsInt = LIVE;
            } else if (mode.equalsIgnoreCase("edit")) {
                modeAsInt = EDIT;
            } else if (mode.equalsIgnoreCase("preview")) {
                modeAsInt = PREVIEW;
            }
        }
        EditActions.switchMode(linker,modeAsInt);
    }

}
