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
package org.jahia.ajax.gwt.client.widget.versioning.comparison;

import com.google.gwt.user.client.ui.*;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaRawField;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 mai 2008
 * Time: 14:42:20
 * To change this template use File | Settings | File Templates.
 */
public class FieldView extends Composite {

    private GWTJahiaRawField field;
    private VerticalPanel fieldPanel;
    private HorizontalPanel titleBar;
    private Label fieldTitleLabel;
    private HTML fieldContent;
    private VersionComparisonView versionComparison;

    public FieldView(GWTJahiaRawField field,VersionComparisonView versionComparisonView){
        this.field = field;
        this.versionComparison = versionComparisonView;
        this.initWidget();
    }

    public GWTJahiaRawField getField() {
        return field;
    }

    public void setField(GWTJahiaRawField field) {
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
