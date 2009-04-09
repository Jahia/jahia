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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.search;

import java.util.ArrayList;
import java.util.List;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.content.events.ContentActivationEvent;
import org.jahia.content.events.ContentObjectDeleteEvent;
import org.jahia.content.events.ContentObjectRestoreVersionEvent;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.engines.addcontainer.AddContainer_Engine;
import org.jahia.engines.deletecontainer.DeleteContainer_Engine;
import org.jahia.engines.updatecontainer.UpdateContainer_Engine;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SerializableParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.search.indexingscheduler.impl.condition.ActionRuleCondition;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.timebasedpublishing.RetentionRuleEvent;
import org.jahia.services.workflow.WorkflowEvent;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 12 oct. 2005
 * Time: 16:44:37
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSearchListener extends JahiaEventListener {

    /** logging */
     private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaSearchListener.class);

    public void beforeServicesLoad( JahiaEvent je ) { return; }
    public void afterServicesLoad( JahiaEvent je ) { return; }
    public void beforeFieldActivation( JahiaEvent je ) { return; }

    private ThreadLocal<List<ObjectKey>> actionAggregatedEvents = new ThreadLocal<List<ObjectKey>>();
    private ThreadLocal<JahiaEvent> actionEvent = new ThreadLocal<JahiaEvent>();

    public void fieldAdded( JahiaEvent je ) {
        // handled by later objectChanged event
        return;
    }
    public void fieldUpdated( JahiaEvent je ) {
        // handled by later objectChanged event
        return;
    }
    public void fieldDeleted( JahiaEvent je ) {
        // handled by later objectChanged event
        return;
    }

    public void beforeContainerActivation( JahiaEvent je ){ return; }

    public void addContainerEngineBeforeSave( JahiaEvent je ) {
        return;
    }
    public void addContainerEngineAfterInit( JahiaEvent je ) {
        startRecordActionEvent(je);
    }

    public void updateContainerEngineBeforeSave( JahiaEvent je ) { return; }

    public void updateContainerEngineAfterInit( JahiaEvent je ) {
        startRecordActionEvent(je);
    }

    public void containerAdded( JahiaEvent je ) {
        containerUpdated(je);
    }

    public void containerUpdated( JahiaEvent je ) {
        JahiaContainer container = (JahiaContainer)je.getObject();
        if ( container == null || je.getProcessingContext() == null ){
            return;
        }
        if (this.isInActionEvent()){
            recordActionAggregatedEvent(je);
        } else {
            try {
                ContentContainer contentContainer = ContentContainer.getContainer(container.getID());
                String actionPerformed = this.getCurrentActionPerformed();
                RuleEvaluationContext ctx = new RuleEvaluationContext(contentContainer.getObjectKey(),
                        contentContainer,je.getProcessingContext(),je.getProcessingContext().getUser(),
                        actionPerformed,false,false);
                ServicesRegistry.getInstance().getJahiaSearchService()
                    .indexContainer(container.getID(),je.getProcessingContext().getUser(), ctx);
            } catch ( Exception t){
                logger.debug("Exception invoking search indexation service", t);
            }
        }
    }

    public void containerDeleted( JahiaEvent je ) {
    }

    public void pageAdded( JahiaEvent je ) {
        JahiaPage page = (JahiaPage)je.getObject();
        if ( page == null ){
            return;
        }
        if (this.isInActionEvent()){
            recordActionAggregatedEvent(je);
        } else {
            ContentPage contentPage = (ContentPage)ContentPage.getChildInstance(String.valueOf(page.getID()));
            String actionPerformed = this.getCurrentActionPerformed();
            RuleEvaluationContext ctx = new RuleEvaluationContext(contentPage.getObjectKey(),
                    contentPage,je.getProcessingContext(),je.getProcessingContext().getUser(),actionPerformed,false,false);
            ServicesRegistry.getInstance().getJahiaSearchService()
                    .indexPage(page.getID(),je.getProcessingContext().getUser(),ctx);
        }
    }

    public void pageLoaded( JahiaEvent je) { return; }

    public void pagePropertiesSet( JahiaEvent je ) {
        JahiaPage page = (JahiaPage)je.getObject();
        if ( page == null ){
            return;
        }
        ContentPage contentPage = (ContentPage)ContentPage.getChildInstance(String.valueOf(page.getID()));
        String actionPerformed = this.getCurrentActionPerformed();
        RuleEvaluationContext ctx = new RuleEvaluationContext(contentPage.getObjectKey(),
                contentPage,je.getProcessingContext(),je.getProcessingContext().getUser(),actionPerformed,false,false);
        ServicesRegistry.getInstance().getJahiaSearchService()
                .indexPage(page.getID(),je.getProcessingContext().getUser(),ctx);

    }

    public void containerListPropertiesSet( JahiaEvent je ) {
        JahiaContainerList ctnList = (JahiaContainerList)je.getObject();
        if ( ctnList == null ){
            return;
        }
        ServicesRegistry.getInstance().getJahiaSearchService()
                .indexContainerList(ctnList.getID(),je.getProcessingContext().getUser());
    }

    public void templateUpdated( JahiaEvent je ) { return; }

    public void rightsSet( JahiaEvent je) { return; }

    public void userLoggedIn( JahiaEvent je ) { return; }
    public void userLoggedOut( JahiaEvent je ) { return; }

    public void objectChanged( WorkflowEvent je ) {
        ContentObject contentObject = (ContentObject)je.getObject();
        if ( contentObject == null ){
            return;
        }
        if (this.isInActionEvent()){
            recordActionAggregatedEvent(je);
        } else {
            ProcessingContext context = je.getProcessingContext();
            if ( context == null ){
                context = Jahia.getThreadParamBean();
            }
            if ( context == null ){
                try {
                    JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
                            .getSite(contentObject.getSiteID());
                    context = new SerializableParamBean(site,je.getUser());
                } catch ( Exception t ){
                    logger.debug("Exception creating SerializableParamBean",t);
                }
            }
            if ( context != null ){
                String actionPerformed = this.getCurrentActionPerformed();
                RuleEvaluationContext ctx = new RuleEvaluationContext(contentObject.getObjectKey(),
                    contentObject,context,je.getUser(), actionPerformed, false, false);
                ServicesRegistry.getInstance().getJahiaSearchService()
                .indexContentObject(contentObject, je.getUser(), ctx);
            }
        }
    }

    public void beforeStagingContentIsDeleted(JahiaEvent je ) {
        // handled by later objectChanged event
        return;
    }

    public void metadataEngineAfterInit (JahiaEvent theEvent) { return; }

    public void metadataEngineBeforeSave (JahiaEvent theEvent) { return; }

    public void metadataEngineAfterSave (JahiaEvent theEvent) {
        // handled by sub-sequent objectChanged event
        return;
    }

    public void afterGroupActivation (ContentActivationEvent theEvent) { return; }

    public void contentActivation (ContentActivationEvent theEvent) {
        ContentObject contentObject = (ContentObject)theEvent.getSource();
        if ( contentObject == null ){
            return;
        }
        if (this.isInActionEvent()){
            recordActionAggregatedEvent(theEvent);
        } else {
            String actionPerformed = this.getCurrentActionPerformed();
            RuleEvaluationContext ctx = new RuleEvaluationContext(contentObject.getObjectKey(),
                    contentObject,theEvent.getProcessingContext(),theEvent.getProcessingContext().getUser(),
                    actionPerformed,false, false);
            ServicesRegistry.getInstance().getJahiaSearchService()
                    .indexContentObject(contentObject, theEvent.getUser(), ctx);
        }
    }

    public void contentObjectCreated (JahiaEvent theEvent) {
        // handled by sub-sequent objectChanged and pageAdded event
        return;
    }

    public void contentObjectUpdated (JahiaEvent theEvent) {
        ContentObject contentObject = (ContentObject)theEvent.getObject();
        if ( contentObject == null ){
            return;
        }
        if (this.isInActionEvent()){
            recordActionAggregatedEvent(theEvent);
        } else {
            ProcessingContext context = theEvent.getProcessingContext();
            if ( context == null ){
                context = Jahia.getThreadParamBean();
            }
            if ( context != null ){
                String actionPerformed = this.getCurrentActionPerformed();
                RuleEvaluationContext ctx = new RuleEvaluationContext(contentObject.getObjectKey(),
                        contentObject,theEvent.getProcessingContext(),theEvent.getProcessingContext().getUser(),
                        actionPerformed,false,false);
                ServicesRegistry.getInstance().getJahiaSearchService()
                    .indexContentObject(contentObject, theEvent.getProcessingContext().getUser(), ctx);
            }
        }
    }

    public void contentObjectUndoStaging (ContentUndoStagingEvent theEvent) {
        ContentObject contentObject = (ContentObject)theEvent.getObject();
        if ( contentObject == null ){
            return;
        }
        String actionPerformed = this.getCurrentActionPerformed();
        try {
            if ( contentObject.hasActiveOrStagingEntries() ){
                if (this.isInActionEvent()){
                    recordActionAggregatedEvent(theEvent);
                } else {
                    RuleEvaluationContext ctx = new RuleEvaluationContext(contentObject.getObjectKey(),
                            contentObject,theEvent.getProcessingContext(),theEvent.getProcessingContext().getUser(),actionPerformed,
                            false,false);
                    ServicesRegistry.getInstance().getJahiaSearchService()
                        .indexContentObject(contentObject, theEvent.getProcessingContext().getUser(), ctx);
                }
            } else {
                RuleEvaluationContext ctx = new RuleEvaluationContext(contentObject.getObjectKey(),
                        contentObject,theEvent.getProcessingContext(),theEvent.getProcessingContext().getUser());
                ServicesRegistry.getInstance().getJahiaSearchService()
                        .removeFromSearchEngine(theEvent.getSiteId(),
                                JahiaSearchConstant.OBJECT_KEY,contentObject.getObjectKey().toString(),
                                theEvent.getProcessingContext().getUser(),ctx);
            }
        } catch ( Exception t ){
            logger.debug("Exception on undo staging event",t);
        }
    }

    public void contentObjectRestoreVersion (ContentObjectRestoreVersionEvent theEvent) {
        ContentObject contentObject = (ContentObject)theEvent.getObject();
        if ( contentObject == null ){
            return;
        }
        if (this.isInActionEvent()){
            recordActionAggregatedEvent(theEvent);
        } else {
            String actionPerformed = this.getCurrentActionPerformed();
            RuleEvaluationContext ctx = new RuleEvaluationContext(contentObject.getObjectKey(),
                    contentObject,theEvent.getProcessingContext(),theEvent.getProcessingContext().getUser(),actionPerformed,
                    false,false);
            ServicesRegistry.getInstance().getJahiaSearchService()
                    .indexContentObject(contentObject, theEvent.getProcessingContext().getUser(),ctx);
        }
    }

    public void contentObjectDelete (ContentObjectDeleteEvent theEvent) {
        ContentObject contentObject = (ContentObject)theEvent.getObject();
        if ( contentObject == null ){
            return;
        }
        RuleEvaluationContext ctx = new RuleEvaluationContext(contentObject.getObjectKey(),
                contentObject,theEvent.getProcessingContext(),theEvent.getProcessingContext().getUser());
        ServicesRegistry.getInstance().getJahiaSearchService()
                .indexContentObject(contentObject, theEvent.getProcessingContext().getUser(),ctx);
    }

    public void fileManagerAclChanged (JahiaEvent theEvent) { return; }

    public void timeBasedPublishingEvent( RetentionRuleEvent theEvent ) { return; }

    /**
     * Event fired to notify all aggregated events will be processed.
     * The event's object is the list of all aggregated events.
     *
     * @param theEvent
     */
    public void aggregatedEventsFlush(JahiaEvent theEvent){
        flushActionEvent();
    }

    private void startRecordActionEvent(JahiaEvent je){
        Object actionEventSource = je.getSource();
        if (actionEventSource==null){
            return;
        }
        this.actionEvent.set(je);
        List<ObjectKey> events = new ArrayList<ObjectKey>();
        this.actionAggregatedEvents.set(events);
    }

    private void recordActionAggregatedEvent(JahiaEvent je){
        if (je ==null){
            return;
        }
        Object eventObject = je.getObject();
        if (eventObject == null){
            return;
        }
        if ( this.actionAggregatedEvents.get()==null ){
            return;
        }
        ObjectKey objectKey = null;
        if (eventObject instanceof JahiaContainer){
            JahiaContainer jahiaContainer = (JahiaContainer)eventObject;
            objectKey = new ContentContainerKey(jahiaContainer.getID());
        } else if (eventObject instanceof ContentContainer){
            objectKey = ((ContentContainer)eventObject).getObjectKey();
        } else if (eventObject instanceof JahiaPage){
            JahiaPage jahiaPage = (JahiaPage)eventObject;
            objectKey = new ContentPageKey(jahiaPage.getID());
        } else if (eventObject instanceof ContentPage){
            objectKey = ((ContentPage)eventObject).getObjectKey();
        }
        if (objectKey != null){
            List<ObjectKey> events = this.actionAggregatedEvents.get();
            if (!events.contains(objectKey)){
                events.add(objectKey);
            }
        }
    }

    private void flushActionEvent(){
        if ( this.actionEvent.get() == null){
            this.actionAggregatedEvents.remove();
            return;
        }
        if (this.actionAggregatedEvents.get() == null){
            this.actionEvent.remove();
            return;
        }
        List<ObjectKey> events = this.actionAggregatedEvents.get();
        JahiaEvent je = (JahiaEvent)this.actionEvent.get();
        String actionPerformed = getActionPerformed(je);
        if (actionPerformed == null){
            this.actionAggregatedEvents.remove();
            this.actionEvent.remove();
            return;
        }
        Object eventObject = je.getObject();
        if (eventObject == null){
            this.actionAggregatedEvents.remove();
            this.actionEvent.remove();
            return;
        }
        ContentObject contentObject = null;
        if (eventObject instanceof ContentObject){
            contentObject = (ContentObject)eventObject;
        } else if (eventObject instanceof JahiaContainer){
            JahiaContainer jahiaContainer = (JahiaContainer)eventObject;
            contentObject = jahiaContainer.getContentContainer();
        } else if (eventObject instanceof JahiaPage){
            contentObject = ((JahiaPage)eventObject).getContentPage();
        }
        if (contentObject == null){
            this.actionEvent.remove();
            this.actionAggregatedEvents.remove();
            return;
        }
        RuleEvaluationContext ctx = null;
        for (ObjectKey objectKey : events){
            try {
                contentObject = ContentObject.getContentObjectInstance(objectKey);
                ctx = new RuleEvaluationContext(objectKey,
                        contentObject,je.getProcessingContext(),je.getProcessingContext().getUser(),actionPerformed,true
                        ,false);
                ServicesRegistry.getInstance().getJahiaSearchService()
                        .indexContentObject(contentObject, je.getProcessingContext().getUser(), ctx);
            } catch (Exception t){
                logger.debug("Exception indexing content",t);
            }
        }
        this.actionAggregatedEvents.remove();
        this.actionEvent.remove();
    }

    private String getActionPerformed(JahiaEvent je){
        String actionPerformed = null;
        if (je.getSource().getClass().getName().equals(UpdateContainer_Engine.class.getName())){
            actionPerformed = ActionRuleCondition.UPDATE_ENGINE;
        } else if (je.getSource().getClass().getName().equals(AddContainer_Engine.class.getName())){
            actionPerformed = ActionRuleCondition.ADD_ENGINE;
        }  else if (je.getSource().getClass().getName().equals(DeleteContainer_Engine.class.getName())){
            actionPerformed = ActionRuleCondition.DELETE_ENGINE;
        } else if (je.getSource().getClass().getName().toLowerCase().indexOf("form") > -1){
            actionPerformed = ActionRuleCondition.STORE_FORM_IN_TEMPLATE;
        }
        return actionPerformed;
    }

    private String getCurrentActionPerformed(){
        if ( this.actionEvent.get() == null){
            return null;
        }
        return getActionPerformed((JahiaEvent)this.actionEvent.get());
    }

    private boolean isInActionEvent(){
        return (this.actionEvent.get() != null);
    }
}
