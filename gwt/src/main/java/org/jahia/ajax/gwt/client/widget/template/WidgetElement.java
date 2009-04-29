/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.template;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 24 sept. 2007
 * Time: 15:01:59
 * To change this template use File | Settings | File Templates.
 */
public class WidgetElement extends Widget {
    private String header;
    private String id;

    public WidgetElement(Element ele) {
        super();
        setElement(ele);
        setHeader(ele.getTitle());
        setWidth("100%");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String toString(){
        return "WidgetElement["+getId()+"]";
    }
}
