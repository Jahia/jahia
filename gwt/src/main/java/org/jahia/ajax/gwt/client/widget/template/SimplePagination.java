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
