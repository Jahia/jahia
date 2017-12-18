/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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