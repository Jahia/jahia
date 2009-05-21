/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.template;

import com.google.gwt.user.client.ui.*;

/**
 * User: jahia
 * Date: 11 janv. 2008
 * Time: 15:05:24
 */
public abstract class SimplePagination extends Composite {
    private HTML goToFirstPageLabel = new HTML("&nbsp;");
    private HTML goToPreviousPage = new HTML("&nbsp;");
    private HTML pagesLabel = new HTML(" pages ");
    private HTML goToNextPage = new HTML("&nbsp;");
    private HTML goToLastPage = new HTML("&nbsp;");
    private TextBox currentPageTextBox = new TextBox();
    private int pagesNumber = 0;
    private int currentPageIndex = 0;

    public SimplePagination() {
        init();
    }

    public SimplePagination(int pagesNumber) {
        this.pagesNumber = pagesNumber;
        init();

    }

    private void init() {
        /*  << < page 3 of 21 > >> */
        HorizontalPanel mainPanel = new HorizontalPanel();
        goToFirstPageLabel.setStyleName("jahia-gwt-simplepagination-firstpage");
        goToFirstPageLabel.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                onFirstPageClick();
            }
        });
        mainPanel.add(goToFirstPageLabel);

        mainPanel.add(goToPreviousPage);
        goToPreviousPage.setStyleName("jahia-gwt-simplepagination-previouspage");
        goToPreviousPage.setWidth("15px");
        goToPreviousPage.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                onPreviousPageClick();
            }
        });

        mainPanel.add(pagesLabel);

        mainPanel.add(currentPageTextBox);
        currentPageTextBox.addStyleName("jahia-gwt-simplepagination-label");
        currentPageTextBox.setText(String.valueOf(currentPageIndex+1));
        currentPageTextBox.setVisibleLength(2);

        Label totalPagesLabel = new Label(" of " + pagesNumber);
        totalPagesLabel.addStyleName("jahia-gwt-simplepagination-label");
        mainPanel.add(totalPagesLabel);

        mainPanel.add(goToNextPage);
        goToNextPage.setStyleName("jahia-gwt-simplepagination-nextpage");
        goToNextPage.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                onNextPageClick();
            }
        });

        mainPanel.add(goToLastPage);
        goToLastPage.setStyleName("jahia-gwt-simplepagination-lastpage");
        goToLastPage.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                onLastPageClick();
            }
        });

        initWidget(mainPanel);
    }

    public int getPagesNumber() {
        return pagesNumber;
    }

    public void setPagesNumber(int pagesNumber) {
        this.pagesNumber = pagesNumber;
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public void onFirstPageClick() {
        currentPageIndex = 0;
        currentPageTextBox.setText(String.valueOf(currentPageIndex + 1));
        updateUI();
    }

    public void onPreviousPageClick() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            currentPageTextBox.setText(String.valueOf(currentPageIndex + 1));
            updateUI();
        }
    }

    public void onNextPageClick() {
        if (currentPageIndex < (pagesNumber - 1)) {
            currentPageIndex++;
            currentPageTextBox.setText(String.valueOf(currentPageIndex + 1));
            updateUI();
        }
    }

    public void onLastPageClick() {
        currentPageIndex = pagesNumber - 1;
        currentPageTextBox.setText(String.valueOf(currentPageIndex + 1));
        updateUI();
    }

    public abstract void updateUI();


}
