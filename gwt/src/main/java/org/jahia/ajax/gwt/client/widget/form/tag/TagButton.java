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
package org.jahia.ajax.gwt.client.widget.form.tag;

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

/**
 * Provide a tag representation
 *
 * @author kevan
 */
public class TagButton extends HorizontalPanel {
    private Field<String> tagField;
    private Button tagButton;

    public TagButton(String tag) {
        super();
        this.setBorders(true);
        this.setSpacing(2);
        this.setStyleAttribute("marginRight", "5px");
        this.setStyleAttribute("marginBottom", "5px");
        this.setStyleAttribute("backgroundColor", "white");
        tagButton = new Button();
        tagButton.setIcon(StandardIconsProvider.STANDARD_ICONS.delete());
        Text tagText = new Text(tag);
        tagText.setStyleAttribute("margin", "4px");
        add(tagText);
        add(tagButton);
        this.tagField = new TextField<String>();
        this.tagField.setValue(tag);
    }

    public Field<String> getTagField() {
        return tagField;
    }

    public Button getTagButton() {
        return tagButton;
    }
}
