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
package org.jahia.services.search.indexingscheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 24 ao�t 2007
 * Time: 09:49:13
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSearchIndexationService extends JahiaService {

    private static Logger logger = Logger
            .getLogger(JahiaSearchIndexationService.class);

    private static JahiaSearchIndexationService service;

    private IndexationRuleInterface defaultContentIndexationRule;

    private IndexationRuleInterface defaultFileFieldIndexationRule;

    private List<IndexationRuleInterface> contentIndexationRules = new ArrayList<IndexationRuleInterface>();

    private List<IndexationRuleInterface> fileFieldIndexationRules = new ArrayList<IndexationRuleInterface>();

    /**
     * Returns an instance of this service.
     *
     * @return an instance of this service
     */
    public static JahiaSearchIndexationService getInstance() {
        if (service == null) {
            synchronized (JahiaSearchIndexationService.class) {
                if (service == null) {
                    service = new JahiaSearchIndexationService();
                }
            }
        }
        return service;
    }

    public void start() throws JahiaInitializationException {
    }

    public void stop() throws JahiaException {
    }

    /**
     * Returns the default indexation rule for Jahia Content
     * @return
     */
    public IndexationRuleInterface getDefaultContentIndexationRule() {
        return defaultContentIndexationRule;
    }

    public void setDefaultContentIndexationRule(IndexationRuleInterface defaultContentIndexationRule) {
        this.defaultContentIndexationRule = defaultContentIndexationRule;
    }

    /**
     * Returns the default indexation rule for Jahia File Field with PDF or Office word
     * @return
     */
    public IndexationRuleInterface getDefaultFileFieldIndexationRule() {
        return defaultFileFieldIndexationRule;
    }

    public void setDefaultFileFieldIndexationRule(IndexationRuleInterface defaultFileFieldIndexationRule) {
        this.defaultFileFieldIndexationRule = defaultFileFieldIndexationRule;
    }

    /**
     * Returns the list of indexation rules to apply to Jahia Content
     * @return
     */
    public List<IndexationRuleInterface> getContentIndexationRules(){
        synchronized(this.contentIndexationRules){
            if (this.contentIndexationRules.isEmpty() && this.defaultContentIndexationRule != null){
                this.contentIndexationRules.add(this.defaultContentIndexationRule);
            }
        }
        return this.contentIndexationRules;
    }

    public void setContentIndexationRules(List<IndexationRuleInterface> contentIndexationRules){
        synchronized(this.contentIndexationRules){
            this.contentIndexationRules = contentIndexationRules;
        }
    }

    /**
     * Returns the the list of indexation rules to apply to Jahia File Field
     * @return
     */
    public List<IndexationRuleInterface> getFileFieldIndexationRules() {
        synchronized(this.fileFieldIndexationRules){
            if (this.fileFieldIndexationRules.isEmpty() && this.defaultFileFieldIndexationRule != null){
                this.fileFieldIndexationRules.add(this.defaultFileFieldIndexationRule);
            }
        }
        return fileFieldIndexationRules;
    }

    public void setFileFieldIndexationRules(List<IndexationRuleInterface> fileFieldIndexationRules) {
        synchronized(this.fileFieldIndexationRules){
            this.fileFieldIndexationRules = fileFieldIndexationRules;
        }
    }

    /**
     * evaluate indexation policies and return the last Rule that matches
     * the given ContentObject.
     *
     * @param contentObject
     * @param context
     * @param user
     * @return
     */
    public IndexationRuleInterface evaluateContentIndexationRules(ContentObject contentObject,
                                                                  ProcessingContext context,
                                                                  JahiaUser user)
    {
        return evaluateContentIndexationRules(new RuleEvaluationContext(contentObject.getObjectKey(),
                contentObject,context,user));
    }

    /**
     * evaluate indexation policies and return the last Rule that matches
     * the given ContentObject.
     *
     * @param ctx
     * @return
     */
    public IndexationRuleInterface evaluateContentIndexationRules(RuleEvaluationContext ctx)
    {
        if ( ctx == null ){
            return null;
        }
        IndexationRuleInterface lastMatchedRule = null;
        if ( ctx instanceof FileFieldRuleEvaluationContext ){
            this.getFileFieldIndexationRules();

            synchronized(fileFieldIndexationRules){
                for (IndexationRuleInterface rule : fileFieldIndexationRules){
                    if ( rule.enabled() ){
                        try {
                            lastMatchedRule = rule.evaluate(ctx,lastMatchedRule);
                        } catch (Exception t) {
                            logger.debug("Exception occurred when evaluating indexation rule id=" + rule.getId() + ", namedID="
                                    + rule.getNamedID(),t);
                        }
                    }
                }
            }
        } else {
            this.getContentIndexationRules();
            synchronized(contentIndexationRules){
                for (IndexationRuleInterface rule : contentIndexationRules){
                    if ( rule.enabled() ){
                        try {
                            lastMatchedRule = rule.evaluate(ctx,lastMatchedRule);
                        } catch (Exception t) {
                            logger.debug("Exception occurred when evaluating indexation rule id=" + rule.getId() + ", namedID="
                                    + rule.getNamedID(),t);
                        }
                    }
                }
            }
        }
        return lastMatchedRule;
    }
}
