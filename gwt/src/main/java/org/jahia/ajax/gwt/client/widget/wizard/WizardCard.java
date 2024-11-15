/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.widget.wizard;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
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
public abstract class WizardCard extends LayoutContainer {

    private List<Listener<BaseEvent>> finishListeners;
    private String cardtitle;
    private FormPanel panel;
    private WizardWindow wizardWindow;
    private String htmltext;
    private boolean uiCreated;
    private WizardCard nextWizardCard;
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

    /**
     * Creates a new wizard card.
     *
     * @param cardtitle title string of this card
     * @param text he car text
     */
    public WizardCard(String cardtitle, String text) {
        this(cardtitle);
        setHtmlText(text);
    }

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);
    }

    protected void notifyFinishListeners() {
        if (finishListeners != null) {
            for (Listener<BaseEvent> listener : finishListeners) {
                listener.handleEvent(new BaseEvent(this));
            }
        }
    }

    /**
     * Returns the currently set title.
     *
     * @return the current title of this card
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
        RowData rowData = new RowData();
        rowData.setHeight(300);
        panel.setFrame(false);
        panel.setBorders(false);
        panel.setBodyBorder(false);
        panel.setHeaderVisible(false);
        add(panel,rowData);
    }


    /**
     * Calls the isValid of the form (if set) and returns the result.
     *
     * @return the result of the form isValid(), or true if no form set
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

    public void refreshLayout(){
      layout();
    }

    public abstract void next();

    public boolean isUiCreated() {
        return uiCreated;
    }

    public void setUiCreated(boolean uiCreated) {
        this.uiCreated = uiCreated;
    }

    public abstract void createUI();

    public void resetUI() {
        setUiCreated(false);
    }

    public WizardCard getNextCard() {
        return nextWizardCard;

    }

    public void setNextWizardCard(WizardCard nextWizardCard) {
        this.nextWizardCard = nextWizardCard;
    }

}
