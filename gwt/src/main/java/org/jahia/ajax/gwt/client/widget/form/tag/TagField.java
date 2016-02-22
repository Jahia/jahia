/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 *  Provide the GWT Tag field
 *
 * @author kevan
 */
public class TagField extends MultiField<List<String>> {
    protected LayoutContainer displayTagContainer;
    protected AddTagContainer addTagContainer;
    private String separator;

    public TagField(String separator, String autocomplete) {
        super();
        this.separator = separator;
        displayTagContainer = new LayoutContainer(new ColumnLayout());
        displayTagContainer.setScrollMode(Style.Scroll.AUTOY);
        displayTagContainer.setWidth("100%");
        addTagContainer = new AddTagContainer(this, autocomplete);
        addTagContainer.setSpacing(5);
    }

    @Override
    public List<String> getValue() {
        return getTags();
    }

    @Override
    public void setValue(List<String> value) {
        for (String tag : value){
            addSplitedTag(tag);
        }
    }

    @Override
    public void setRawValue(String value) {
        // Cannot set raw value
    }

    @Override
    protected void onRender(Element target, int index) {
        lc = new VerticalPanel();
        if (GXT.isIE) {
            lc.setStyleAttribute("position", "relative");
        }
        lc.add(displayTagContainer);
        lc.add(addTagContainer);
        lc.render(target, index);
        ComponentHelper.setParent(this, lc);
        setElement(lc.getElement());

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                lc.setWidth(getWidth());
                displayTagContainer.setWidth(getWidth());
                addTagContainer.setWidth(getWidth());
                lc.layout();
            }
        });
    }

    protected void addTag(String tag){
        if(tag != null && tag.length() > 0){
            if((separator != null && separator.length() > 0)){
                for (String subTag : tag.split(separator)){
                    addSplitedTag(subTag);
                }
            }else {
                addSplitedTag(tag);
            }
        }
    }

    private void addSplitedTag(String tag){
        if(tag != null && tag.trim().length() > 0){
            JahiaContentManagementService.App.getInstance().convertTag(tag, new BaseAsyncCallback<String>() {
                @Override
                public void onSuccess(String tag) {
                    boolean exist = false;
                    for (Component tagComponent : displayTagContainer.getItems()){
                        if(tagComponent instanceof TagButton && tag.equals(((TagButton) tagComponent).getTagField().getValue())){
                            exist = true;
                            break;
                        }
                    }
                    if(!exist){
                        TagButton tagButton = new TagButton(tag);
                        tagButton.getTagButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
                            @Override
                            public void componentSelected(ButtonEvent ce) {
                                displayTagContainer.remove(ce.getButton().getParent());
                                displayTagContainer.layout(true);
                            }
                        });
                        if (afterRender) {
                            displayTagContainer.insert(tagButton, displayTagContainer.getItems().size());
                            displayTagContainer.layout(true);
                        } else {
                            displayTagContainer.add(tagButton);
                        }
                    }
                }
            });
        }
    }

    protected List<String> getTags(){
        List<String> tags = new ArrayList<String>();
        for (Component tagComponent : displayTagContainer.getItems()){
            if(tagComponent instanceof TagButton){
                tags.add(String.valueOf(((TagButton) tagComponent).getTagField().getValue()));
            }
        }
        return tags;
    }
}