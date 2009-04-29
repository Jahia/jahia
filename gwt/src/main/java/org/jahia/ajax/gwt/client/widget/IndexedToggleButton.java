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
package org.jahia.ajax.gwt.client.widget;

import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ClickListener;

/**
 * This is a ToggleButton with an index value.
 * This button is used by <code>ToggleButtonsWidget</code> to handle multiple linked toggle buttons.
 *
 * User: Administrator
 * Date: 14 juil. 2008
 * Time: 22:02:38
 * To change this template use File | Settings | File Templates.
 */
public class IndexedToggleButton extends ToggleButton {

    private int index;

    public IndexedToggleButton(int index) {
        this.index = index;
    }

    public IndexedToggleButton(Image image, int index) {
        super(image);
        this.index = index;
    }

    public IndexedToggleButton(Image image, ClickListener clickListener, int index) {
        super(image, clickListener);
        this.index = index;
    }

    public IndexedToggleButton(Image image, Image image1, int index) {
        super(image, image1);
        this.index = index;
    }

    public IndexedToggleButton(Image image, Image image1, ClickListener clickListener, int index) {
        super(image, image1, clickListener);
        this.index = index;
    }

    public IndexedToggleButton(String s, int index) {
        super(s);
        this.index = index;
    }

    public IndexedToggleButton(String s, ClickListener clickListener, int index) {
        super(s, clickListener);
        this.index = index;
    }

    public IndexedToggleButton(String s, String s1, int index) {
        super(s, s1);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}

