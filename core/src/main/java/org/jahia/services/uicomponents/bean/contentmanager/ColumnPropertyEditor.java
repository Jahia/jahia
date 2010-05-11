package org.jahia.services.uicomponents.bean.contentmanager;


import org.apache.commons.lang.StringUtils;

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
        String[] values = text.split(",");
        if (values[0].contains(".")) {
            c.setKey(StringUtils.substringAfter(values[0],"."));
            c.setDeclaringNodeType(StringUtils.substringBefore(values[0],"."));
        } else {
            c.setKey(values[0]);
        }

        if (values.length > 1) {
            c.setSize(Integer.parseInt(values[1]));
        }
        if (values.length > 2) {
            c.setTitleKey(values[2]);
        }

        setValue(c);
    }

    
}
