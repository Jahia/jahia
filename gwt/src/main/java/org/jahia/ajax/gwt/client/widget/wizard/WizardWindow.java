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


import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonBarEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
 * wizwin.setHeading("A simple example for a wizard");</br>
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

    private String previousButtonText = "< Previous";
    private String nextButtonText = "Next >";
    protected String cancelButtonText = "Cancel";
    private String finishButtonText = "Finish";
    private String indicateStepText = "Step ";
    private String indicateOfText = " of ";

    protected int currentStep = 0;
    protected List<WizardCard> cards;

    private String headerTitle;
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
        this.cards = cards;
        for (WizardCard card : cards) {
            card.setWizardWindow(this);
        }
        setSize(540, 400);
        setClosable(true);
        setResizable(true);
        setModal(true);
    }

    protected void onButtonPressed(Button button) {
        if (button == cancelBtn) {
            hide(button);
            return;
        }
        if (button == prevBtn) {
            if (this.currentStep > 0) {
                currentStep--;
                updateWizard();
            }
        }
        if (button == nextBtn) {
            if (!cards.get(currentStep).isValid()) return;
            if (currentStep + 1 == cards.size()) {
                cards.get(currentStep).notifyFinishListeners();
                hide();
            } else {
                currentStep++;
                updateWizard();
            }
        }
    }

    private void updateWizard() {
        WizardCard wc = cards.get(currentStep);
        headerPanel.updateIndicatorStep(wc.getCardTitle(), wc.getHtmltext());
        this.cardPanel.setActiveItem(wc);
        wc.layout();

        if (currentStep + 1 == cards.size()) {
            nextBtn.setText(finishButtonText);
        } else {
            nextBtn.setText(nextButtonText);
        }

        if (currentStep == 0) {
            prevBtn.setEnabled(false);
        } else {
            prevBtn.setEnabled(true);
        }
    }

    @Override
    protected void onRender(Element parent, int pos) {
        setLayout(new BorderLayout());

        prevBtn = new Button(previousButtonText);
        nextBtn = new Button(nextButtonText);
        cancelBtn = new Button(cancelButtonText);

        buttonBar = new ButtonBar();
        buttonBar.add(prevBtn);
        buttonBar.add(nextBtn);
        buttonBar.add(cancelBtn);
        buttonBar.addListener(Events.Select, new Listener<ButtonBarEvent>() {
            public void handleEvent(ButtonBarEvent bbe) {
                onButtonPressed(bbe.item);
            }
        });
        setButtonBar(buttonBar);

        super.onRender(parent, pos);

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
        return headerTitle;
    }

    /**
     * Sets the title located in the top header
     *
     * @param hdrtitle string value
     */
    public void setHeaderTitle(String hdrtitle) {
        this.headerTitle = hdrtitle;
    }

    /**
     * Sets the progress indicator type. Defaults to DOT
     *
     * @param Indicator value
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

            final String stepStr = indicateStepText + (1 + currentStep) + indicateOfText + cards.size() + " : " + cardtitle;
            final double stepRatio = (double) (1 + currentStep) / (double) cards.size();
            if (description != null) {
                titleHTML.setHtml(description);
            } else {
                titleHTML.setHtml("");
            }


            if (progressIndicator == Indicator.PROGRESSBAR || progressIndicator == Indicator.DOT) {
                DeferredCommand.addCommand(new Command() {
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
        return previousButtonText;
    }

    /**
     * @param previousButtonText the previousButtonText to set. Defaults to "< Previous".
     */
    public void setPreviousButtonText(String previousButtonText) {
        this.previousButtonText = previousButtonText;
    }

    /**
     * @return the nextButtonText
     */
    public String getNextButtonText() {
        return nextButtonText;
    }

    /**
     * @param nextButtonText the nextButtonText to set. Defaults to "Next >".
     */
    public void setNextButtonText(String nextButtonText) {
        this.nextButtonText = nextButtonText;
    }

    /**
     * @return the cancelButtonText
     */
    public String getCancelButtonText() {
        return cancelButtonText;
    }

    /**
     * @param cancelButtonText the cancelButtonText to set. Defaults to "Cancel".
     */
    public void setCancelButtonText(String cancelButtonText) {
        this.cancelButtonText = cancelButtonText;
    }

    /**
     * @return the finishButtonText
     */
    public String getFinishButtonText() {
        return finishButtonText;
    }

    /**
     * @param finishButtonText the finishButtonText to set. Defaults to "Finish".
     */
    public void setFinishButtonText(String finishButtonText) {
        this.finishButtonText = finishButtonText;
    }

    /**
     * @return the indicateStepText
     */
    public String getIndicateStepText() {
        return indicateStepText;
    }

    /**
     * @param indicateStepText the indicateStepText to set. Defaults to "Step ".
     */
    public void setIndicateStepText(String indicateStepText) {
        this.indicateStepText = indicateStepText;
    }

    /**
     * @return the indicateOfText
     */
    public String getIndicateOfText() {
        return indicateOfText;
    }

    /**
     * @param indicateOfText the indicateOfText to set. Defaults to " of ".
     */
    public void setIndicateOfText(String indicateOfText) {
        this.indicateOfText = indicateOfText;
    }

    public List<WizardCard> getCards() {
        return cards;
    }
}