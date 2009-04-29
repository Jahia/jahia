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
package org.jahia.ajax.gwt.client.widget.menu;

import com.extjs.gxt.ui.client.util.WidgetHelper;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 17 juil. 2008
 * Time: 14:30:29
 * To change this template use File | Settings | File Templates.
 */
public class AutoCompletionMenu extends Menu {

    protected DataList choices;

    private AutoCompletionMenuItem item;

    public AutoCompletionMenu() {
      item = new AutoCompletionMenuItem();
      choices = item.choices;
      choices.setWidth(200);
      add(item);
      baseStyle = "x-autocompletion-menu";
      setAutoHeight(true);
      setWidth(200);
    }

    /**
     * Returns the selected choice.
     *
     * @return the date
     */
    public DataListItem getChoice() {
        return item.choices.getSelectedItem();
    }

    public DataList getChoices() {
        return choices;
    }

    @Override
    protected void doAttachChildren() {
      super.doAttachChildren();
      WidgetHelper.doAttach(item.choices);
    }

    @Override
    protected void doDetachChildren() {
      super.doDetachChildren();
      WidgetHelper.doDetach(item.choices);
    }

    public void focus() {
      super.focus();
      if (rendered) {
          if (this.getChoices().getItemCount()>0){
            this.getChoices().getItem(0).focus();
          }
      }
    }
}