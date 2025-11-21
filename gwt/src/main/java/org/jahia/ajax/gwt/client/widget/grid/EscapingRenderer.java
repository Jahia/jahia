/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.ajax.gwt.client.widget.grid;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * A renderer class that escapes HTML content to protect against XSS (Cross-Site Scripting) attacks.
 * It is used to render cell content in a grid by escaping any potentially unsafe HTML characters.
 * <p>
 * Implements the {@link GridCellRenderer} interface for rendering grid cells of {@link GWTJahiaNode} with safely escaped content.
 */
public final class EscapingRenderer implements GridCellRenderer<GWTJahiaNode> {
    private static final EscapingRenderer INSTANCE = new EscapingRenderer();

    private EscapingRenderer() {
        // singleton pattern
    }

    @Override
    public Object render(GWTJahiaNode model, String property, ColumnData config, int rowIndex, int colIndex, ListStore<GWTJahiaNode> store,
            Grid<GWTJahiaNode> grid) {
        Object val = model.get(property);
        return SafeHtmlUtils.htmlEscape(String.valueOf(val));
    }

    public static EscapingRenderer getInstance() {
        return INSTANCE;
    }
}
