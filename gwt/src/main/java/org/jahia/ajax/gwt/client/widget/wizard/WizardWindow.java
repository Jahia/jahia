/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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


import java.util.LinkedList;
import java.util.List;

import org.jahia.ajax.gwt.client.messages.Messages;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Element;

/**
 * A wizard window intended to display wizard cards.</br></br>
 * <p/>
 * // setup an array of WizardCards</br> List<WizardCard> cards = new
 * List<WizardCard>();</br> </br> // 1st card - a welcome</br> WizardCard
 * wc = new WizardCard("Welcome");</br> wc.setHtmlText(
 * "Welcome to the example for <strong>ext.ux.WizardWindow</strong>, "</br> +
 * "a ExtGWT user extension for creating wizards.<br/><br/>"</br> +
 * "Please click the \"next\"-button and fill out all form values.");</br>
 * cards.add(wc);</br> </br> // 2nd or more cards...</br> // wc = new
 * WizardCard("More cards...");</br> // cards.add(wc);</br> // ...</br> </br>
 * WizardWindow wizwin = new WizardWindow(cards);</br>
 * wizwin.setHeadingHtml("A simple example for a wizard");</br>
 * wizwin.setHeaderTitle("Simple Wizard Example");</br> </br>
 * wizwin.show();</br>
 */

public class WizardWindow extends Window {

    /**
     * Indicator type enumeration.
     */
    public enum Indicator {
        NONE, DOT, PROGRESSBAR
    }

    protected int currentStep = 0;
    protected List<WizardCard> cards;

    private Header headerPanel;
    private CardPanel cardPanel;
    protected Button prevBtn;
    protected Button nextBtn;
    protected Button cancelBtn;
    private Indicator progressIndicator = Indicator.DOT;

    /**
     * Creates a new wizard window.
     *
     * @param cards an List of WizardCard/s
     */
    public WizardWindow(List<WizardCard> cards) {
        super();
        addStyleName("wizard-window");
        this.cards = cards != null ? cards : new LinkedList<WizardCard>();
        for (WizardCard card : this.cards) {
            card.setWizardWindow(this);
        }
        setSize(540, 400);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setHeadingHtml(getHeaderTitle());
    }

    protected void onButtonPressed(Button button) {
        if (button == cancelBtn) {
            hide(button);
            return;
        }
        if (button == prevBtn) {
            doPrevious();
        }
        if (button == nextBtn) {
            doNext();
        }
    }

    /**
     * Performs operation on the cancel button click;
     */
    public void doCancel() {
        hide();
    }

    public void doNext() {
        if (!cards.get(currentStep).isValid()) return;

        // execute next action
        cards.get(currentStep).next();

        if (currentStep + 1 == cards.size()) {
            cards.get(currentStep).notifyFinishListeners();
//            hide();
        } else {
            WizardCard nextCard = cards.get(currentStep + 1);
            if (!nextCard.isUiCreated()) {
                nextCard.createUI();
                nextCard.setUiCreated(true);
            }
            currentStep++;
            updateWizard();
        }
    }

    public void doPrevious() {
        if (this.currentStep > 0) {
            currentStep--;
            updateWizard();
        }
    }

    public void updateWizard() {
        WizardCard wc = cards.get(currentStep);
        headerPanel.updateIndicatorStep(wc.getCardTitle(), wc.getHtmltext());
        wc.refreshLayout();
        this.cardPanel.setActiveItem(wc);

        if (currentStep + 1 == cards.size()) {
            nextBtn.setText(getFinishButtonText());
        } else {
            nextBtn.setText(getNextButtonText());
        }

        if (currentStep == 0) {
            prevBtn.setEnabled(false);
        } else {
            prevBtn.setEnabled(true);
        }
    }

    @Override
    protected void onRender(Element parent, int pos) {
        prevBtn = new Button(getPreviousButtonText());
        prevBtn.addStyleName("button-previous");
        nextBtn = new Button(getNextButtonText());
        nextBtn.addStyleName("button-next");
        cancelBtn = new Button(getCancelButtonText());
        cancelBtn.addStyleName("button-cancel");

        SelectionListener<ButtonEvent> listener = new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent buttonEvent) {
                onButtonPressed(buttonEvent.getButton());
            }
        };

        prevBtn.addSelectionListener(listener);
        nextBtn.addSelectionListener(listener);
        cancelBtn.addSelectionListener(listener);

        ToolBar buttonBar = new ToolBar();
        buttonBar.add(prevBtn);
        buttonBar.add(nextBtn);
        buttonBar.add(cancelBtn);
        setBottomComponent(buttonBar);

        super.onRender(parent, pos);
        setLayout(new BorderLayout());

        headerPanel = new Header();
        add(headerPanel, new BorderLayoutData(LayoutRegion.NORTH, 60));
        cardPanel = new CardPanel();
        cardPanel.setStyleAttribute("padding", "5px 5px 5px 5px");
        cardPanel.setStyleAttribute("backgroundColor", "#F6F6F6");

        add(cardPanel, new BorderLayoutData(LayoutRegion.CENTER));
        for (WizardCard wizardCard : cards) {
            cardPanel.add(wizardCard);
        }

        if (cards.size() > 0) {
            updateWizard();
        }
    }

    /**
     * Returns the currently set header title
     *
     * @return the header title
     */
    public String getHeaderTitle() {
        return Messages.get("org.jahia.engines.wizard.title", "Wizard");
    }

    /**
     * Sets the progress indicator type. Defaults to DOT
     *
     * @param value value
     */
    public void setProgressIndicator(Indicator value) {
        progressIndicator = value;
    }


    protected class Header extends VerticalPanel {
        private ProgressBar indicatorBar;
        private HtmlContainer titleHTML;

        protected Header() {
            super();
            setTableWidth("100%");
            setTableHeight("100%");
            setStyleName("ext-ux-wiz-Header");
            setBorders(true);

            titleHTML = new HtmlContainer("");
            titleHTML.setStyleAttribute("font-weight", "bold");
            titleHTML.setStyleAttribute("padding", "4px 0px 0px 4px");
            titleHTML.setStyleName("ext-ux-wiz-Header-title");
            add(titleHTML);

            if (progressIndicator == Indicator.PROGRESSBAR || progressIndicator == Indicator.DOT) {
                indicatorBar = new ProgressBar();
                LayoutContainer lc = new LayoutContainer();
                lc.add(indicatorBar);
                lc.setWidth("50%");
                TableData td = new TableData();
                td.setHorizontalAlign(HorizontalAlignment.RIGHT);
                td.setPadding(5);
                add(lc, td);
            }
        }

        protected void updateIndicatorStep(String cardtitle, String description) {

            final String stepStr = getIndicateStepText() + " " + (1 + currentStep)+ " " + getIndicateOfText() + " " + cards.size() + " : " + cardtitle;
            final double stepRatio = (double) (1 + currentStep) / (double) cards.size();
            if (description != null) {
                titleHTML.setHtml(description);
            } else {
                titleHTML.setHtml("");
            }


            if (progressIndicator == Indicator.PROGRESSBAR || progressIndicator == Indicator.DOT) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        indicatorBar.updateProgress(stepRatio, stepStr);
                    }
                });
            }
        }

        @Override
        protected void onRender(Element parent, int pos) {
            super.onRender(parent, pos);
            setStyleAttribute("borderLeft", "none");
            setStyleAttribute("borderRight", "none");
            setStyleAttribute("borderTop", "none");
        }
    }



    /**
     * @return the previousButtonText
     */
    public String getPreviousButtonText() {
        return Messages.get("org.jahia.engines.wizard.button.prev", "< Previous");
    }

    /**
     * @return the nextButtonText
     */
    public String getNextButtonText() {
        return Messages.get("org.jahia.engines.wizard.button.next", "Next >");
    }

    /**
     * @return the cancelButtonText
     */
    public String getCancelButtonText() {
        return Messages.get("label.cancel", "Cancel");
    }

    /**
     * @return the finishButtonText
     */
    public String getFinishButtonText() {
        return Messages.get("label.finish", "Finish");
    }

    /**
     * @return the indicateStepText
     */
    public String getIndicateStepText() {
        return Messages.get("label.step", "Step");
    }

    /**
     * @return the indicateOfText
     */
    public String getIndicateOfText() {
        return Messages.get("label.of", "of");
    }

    public List<WizardCard> getCards() {
        return cards;
    }

    /**
     * Resets the UI of all cards from the the specified one.
     *
     * @param index
     *            the card index to start after
     */
    public void resetCards(int index) {
        for (int i = index + 1; i < cards.size(); i++) {
            WizardCard card = cards.get(i);
            if (card != null) {
                card.resetUI();
            }
        }
    }

    /**
     * Adds the specified card to the wizard and link the previous one with it.
     *
     * @param card
     *            the wizard card to be added
     * @return this wizard window object
     */
    public WizardWindow addCard(WizardCard card) {
        if (cards.size() > 0) {
            cards.get(cards.size() - 1).setNextWizardCard(card);
        }
        cards.add(card);
        card.setWizardWindow(this);

        return this;
    }

}
