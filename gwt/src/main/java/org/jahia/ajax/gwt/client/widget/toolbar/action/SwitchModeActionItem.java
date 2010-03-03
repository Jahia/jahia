package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 8, 2010
 * Time: 2:09:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwitchModeActionItem extends BaseActionItem {


    @Override
    public void onComponentSelection() {
        final String mode = getPropertyValue(getGwtToolbarItem(), "mode");
        int modeAsInt = Constants.MODE_STAGING;
        if (mode != null) {
            if (mode.equalsIgnoreCase("live")) {
                modeAsInt = Constants.MODE_LIVE;
            } else if (mode.equalsIgnoreCase("edit")) {
                modeAsInt = Constants.MODE_STAGING;
            } else if (mode.equalsIgnoreCase("preview")) {
                modeAsInt = Constants.MODE_PREVIEW;
            }
        }
        EditActions.switchMode(linker,modeAsInt);
    }

}
