package org.jahia.ajax.gwt.client.data.toolbar;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 12, 2010
 * Time: 12:25:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTEngine  implements Serializable {
    public static final int OPERATION_CREATE = 0;
    public static final int OPERATION_EDIT = 1;

    private String nodeType;

    private List<String> tabs;

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public List<String> getTabs() {
        return tabs;
    }

    public void setTabs(List<String> tabs) {
        this.tabs = tabs;
    }


}
