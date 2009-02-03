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

package org.jahia.ajax.gwt.templates.components.actionmenus.client.ui;

import org.jahia.ajax.gwt.commons.client.ui.EngineOpener;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.GWTJahiaState;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents the basic state icon for a content object.
 * 
 * @author Sergiy Shyrkov
 */
public class StateIcon extends HTML {

    /**
     * This changes the css to display the pushed icon.
     */
    private class HoverListener implements MouseListener {

        public void onMouseDown(Widget widget, int i, int i1) {
        }

        public void onMouseEnter(Widget widget) {
            String curStyle = widget.getStyleName();
            widget.setStyleName(curStyle + "h");
        }

        public void onMouseLeave(Widget widget) {
            String curStyle = widget.getStyleName();
            widget.setStyleName(curStyle.substring(0, curStyle.length() - 1));
        }

        // no use
        public void onMouseMove(Widget widget, int i, int i1) {
        }

        public void onMouseUp(Widget widget, int i, int i1) {
        }
    }

    /**
     * Initializes an instance of this class.
     */
    public StateIcon() {
        super("&nbsp;&nbsp;&nbsp;&nbsp;");
        setSize("12px", "12px");
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param state
     *            the state object
     */
    public StateIcon(GWTJahiaState state) {
        this();
        addOnclickListener(state);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param state
     *            the state object
     * @param style
     *            the CSS style to set
     */
    public StateIcon(GWTJahiaState state, String style) {
        this(state, style, false);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param state
     *            the state object
     * @param style
     *            the CSS style to set
     * @param addHover
     *            do we need to add onMouseOver listener to change the icon?
     *            (append -h after css class when hovering)
     */
    public StateIcon(GWTJahiaState state, String style, boolean addHover) {
        this(state);
        setStyleName(style);
        if (addHover){
            addHoverListener();
        }
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param style
     *            the CSS style to set
     */
    public StateIcon(String style) {
        this();
        setStyleName(style);
    }

    protected void addHoverListener() {
        addMouseListener(new HoverListener());
    }

    protected void addOnclickListener(final GWTJahiaState state) {
        if (state.getEngineUrl() != null) {
            addClickListener(new ClickListener() {
                public void onClick(Widget widget) {
                    EngineOpener.openEngine(state.getEngineUrl());
                }
            });
        }
    }
}
