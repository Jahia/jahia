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
package org.jahia.ajax.gwt.client.widget.actionmenu;

import org.jahia.ajax.gwt.client.util.EngineOpener;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaState;

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
