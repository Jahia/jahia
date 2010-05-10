package org.jahia.services.uicomponents.bean.contentmanager;

import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 10, 2010
 * Time: 3:15:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnPropertyEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        Column c = new Column();
        c.setKey(text);
        setValue(c);
    }

    
}
