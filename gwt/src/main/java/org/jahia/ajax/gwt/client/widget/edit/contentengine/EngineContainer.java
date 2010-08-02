package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.ContentPanel;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 4:06:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EngineContainer {
    public ContentPanel getPanel();

    public void setEngine(AbstractContentEngine engine);

    public void showEngine();

    public void closeEngine();

}
