/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
 *
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

