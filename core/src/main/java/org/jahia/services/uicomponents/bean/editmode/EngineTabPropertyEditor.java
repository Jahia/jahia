package org.jahia.services.uicomponents.bean.editmode;

import java.beans.PropertyEditorSupport;

import org.jahia.services.uicomponents.bean.Visibility;

/**
 * Engine tab specific property editor.
 * User: toto
 * Date: May 10, 2010
 * Time: 3:39:09 PM
 */
public class EngineTabPropertyEditor  extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        EngineTab c = new EngineTab();
        String[] values = text.split(",");
        c.setKey(values[0]);
        if (values.length > 1) {
            Visibility v = new Visibility();
            if (values[1].startsWith("$site/")) {
                v.setSiteActionPermission(values[1].substring("$site/".length()));
            } else {
                v.setServerActionPermission(values[1]);
            }
            c.setVisibility(v);
        }
        setValue(c);
    }

}
