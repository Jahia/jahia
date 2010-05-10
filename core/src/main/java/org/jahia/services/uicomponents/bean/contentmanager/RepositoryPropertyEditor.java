package org.jahia.services.uicomponents.bean.contentmanager;

import java.beans.PropertyEditorSupport;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 10, 2010
 * Time: 3:39:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryPropertyEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        Repository c = new Repository();
        c.setKey(text);
        setValue(c);
    }

}
