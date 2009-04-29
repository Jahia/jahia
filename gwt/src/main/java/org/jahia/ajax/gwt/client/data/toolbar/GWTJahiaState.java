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
package org.jahia.ajax.gwt.client.data.toolbar;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 9 avr. 2008
 * Time: 11:44:12
 */
public class GWTJahiaState implements Serializable {
    private int value;
    private int index;
    private int columnIndex;
    private int rowIndex;
    private int pagePositionX;
    private int pagePositionY;
    private boolean display;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPagePositionX() {
        return pagePositionX;
    }

    public void setPagePositionX(int pagePositionX) {
        this.pagePositionX = pagePositionX;
    }

    public int getPagePositionY() {
        return pagePositionY;
    }

    public void setPagePositionY(int pagePositionY) {
        this.pagePositionY = pagePositionY;
    }

    public boolean isDisplay() {
       return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }
}
