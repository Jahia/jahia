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
package org.jahia.ajax.gwt.client.widget.wizard;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Element;

/**
 * Part of wizard window, WizardCard is used to show & validate a step in the
 * wizard process.</br></br>
 * <p/>
 * WizardCard wc = new WizardCard("Welcome");</br> wc.setHtmlText("Welcome to
 * the example for <strong>ext.ux.WizardWindow</strong>, "</br> + "a ExtGWT
 * user extension for creating wizards.<br/><br/>"</br> + "Please click the
 * \"next\"-button and fill out all form values.");</br> cards.add(wc);</br>
 */
public class WizardCard extends LayoutContainer {

    private List<Listener<BaseEvent>> finishListeners;
    private String cardtitle;
    private FormPanel panel;
    private WizardWindow wizardWindow;
    private String htmltext;
    /**
     * Creates a new wizard card.
     *
     * @param cardtitle title string of this card
     */
    public WizardCard(String cardtitle) {
        super();
        this.cardtitle = cardtitle;
        setLayout(new RowLayout(Orientation.VERTICAL));
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
    }

    protected void notifyFinishListeners() {
        if (finishListeners != null) {
            for (Listener<BaseEvent> listener : finishListeners) {
                listener.handleEvent(new BaseEvent());
            }
        }
    }

    /**
     * Returns the currently set title.
     *
     * @returns the current title of this card
     */
    public String getCardTitle() {
        return cardtitle;
    }

    /**
     * Sets the HTML text associated with this card.
     *
     * @param htmltext HTML string to set
     */
    public void setHtmlText(String htmltext) {
        this.htmltext = htmltext;
       // add(new HtmlContainer(htmltext));
    }

    public String getHtmltext() {
        return htmltext;
    }

    /**
     * Sets the FormPanel associated with this card.</br></br> Note: this
     * panel will set height = 300, and the following to false;
     * frame,borders,bodyborder,headervisible
     *
     * @param panel FormPanel to set
     */
    public void setFormPanel(FormPanel panel) {
        this.panel = panel;
        panel.setHeight(300);
        panel.setFrame(false);
        panel.setBorders(false);
        panel.setBodyBorder(false);
        panel.setHeaderVisible(false);
        add(panel);
    }

    /**
     * Calls the isValid of the form (if set) and returns the result.
     *
     * @returns the result of the form isValid(), or true if no form set
     */
    public boolean isValid() {
        if (panel == null) {
            return true;
        }
        return panel.isValid();
    }

    /**
     * Adds a Listener<BaseEvent> to the list of listeners that will be
     * notified when the card is finished.</br> Note: this event is only called
     * on the last card and when the finish button is clicked.
     *
     * @param listener the Listener<BaseEvent> to be added
     */
    public void addFinishListener(Listener<BaseEvent> listener) {
        if (finishListeners == null){ finishListeners = new ArrayList<Listener<BaseEvent>>();}
        finishListeners.add(listener);
    }

    public WizardWindow getWizardWindow() {
        return wizardWindow;
    }

    public void setWizardWindow(WizardWindow wizardWindow) {
        this.wizardWindow = wizardWindow;
    }
    
}