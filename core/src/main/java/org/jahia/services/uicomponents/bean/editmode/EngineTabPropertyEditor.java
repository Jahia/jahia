package org.jahia.services.uicomponents.bean.editmode;

import org.jahia.services.uicomponents.bean.contentmanager.Column;

import java.beans.PropertyEditorSupport;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 10, 2010
 * Time: 3:39:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineTabPropertyEditor  extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        EngineTab c = new EngineTab();
        c.setKey(text);
        setValue(c);
    }

}
