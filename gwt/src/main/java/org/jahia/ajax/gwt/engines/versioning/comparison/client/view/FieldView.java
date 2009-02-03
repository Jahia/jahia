/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.versioning.comparison.client.view;

import com.google.gwt.user.client.ui.*;
import org.jahia.ajax.gwt.engines.versioning.client.model.Field;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 mai 2008
 * Time: 14:42:20
 * To change this template use File | Settings | File Templates.
 */
public class FieldView extends Composite {

    private Field field;
    private VerticalPanel fieldPanel;
    private HorizontalPanel titleBar;
    private Label fieldTitleLabel;
    private HTML fieldContent;
    private VersionComparisonView versionComparison;

    public FieldView(Field field,VersionComparisonView versionComparisonView){
        this.field = field;
        this.versionComparison = versionComparisonView;
        this.initWidget();
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public void setFieldViewContent(String content){
        if (this.fieldContent != null){
            this.fieldContent.setHTML(content);
        }
    }

    private void initWidget(){
        this.fieldPanel = new VerticalPanel();
        this.fieldPanel.setStyleName("gwt-versionComparison-fieldPanel");
        this.fieldPanel.setWidth("100%");
        this.initWidget(this.fieldPanel);

        // init the title bar
        this.titleBar = new HorizontalPanel();
        this.titleBar.addStyleName("gwt-versionComparison-fieldTitleBar");
        this.fieldPanel.add(this.titleBar);

        this.fieldTitleLabel = new Label(this.field.getTitle());
        this.fieldTitleLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        String labelStyleName = "gwt-versionComparison-fieldLabel";
        if (this.field.getIcon() != null && !"".equals(this.field.getIcon().trim())){
            labelStyleName += "-" + this.field.getIcon();
        }
        this.fieldTitleLabel.setStyleName(labelStyleName);

        this.titleBar.add(this.fieldTitleLabel);

        // init the field content
        this.fieldContent = new HTML(this.field.getMergedDiffValue());
        this.fieldContent.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        this.fieldContent.setStyleName("gwt-versionComparison-fieldContent");
        if (this.field.isBigText()){
            ScrollPanel scrollPanel = new ScrollPanel(this.fieldContent);
            this.fieldPanel.add(scrollPanel);
            this.fieldContent.setHeight(this.versionComparison.getBigTextFieldContentHeight());
        } else {
            this.fieldPanel.add(this.fieldContent);
            this.fieldContent.setHeight(this.versionComparison.getFieldContentHeight());
        }
    }

}
