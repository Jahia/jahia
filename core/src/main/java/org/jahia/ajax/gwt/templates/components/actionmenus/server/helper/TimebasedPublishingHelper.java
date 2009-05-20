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
package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingState;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.content.*;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.ContentBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaDateFieldUtil;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaPageField;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.timebasedpublishing.BaseRetentionRule;
import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.servlet.http.HttpServletRequest;
import java.text.Format;
import java.util.Iterator;
import java.util.Locale;

/**
 * Time-based publishing state service.
 *
 * @author rfelden
 * @version 27 f�vr. 2008 - 14:54:15
 */
public class TimebasedPublishingHelper {

    private static Logger logger = Logger.getLogger(TimebasedPublishingHelper.class) ;

    /**
     * Return the url to the timebased publishing icon.
     *
     * @param therequest current request
     * @param jParams processing context
     * @param objectKey the content object key
     * @return the parameters to display the icon
     */
    public static GWTJahiaTimebasedPublishingState getTimebasePublishingState(HttpServletRequest therequest, ProcessingContext jParams, String objectKey) {
        ContentBean contentBean = null;
        try {
            contentBean = JahiaObjectCreator.getContentBeanFromObjectKey(objectKey, jParams);
        } catch (ClassNotFoundException e) {
            logger.error("Timbased publishing state retrieval failed", e);
        } catch (JahiaException e) {
            logger.error("Timbased publishing state retrieval failed", e);
        }

        if (contentBean == null) {
            logger.error("Timbased publishing state retrieval error, bean is null : " + objectKey);
            return null ;
        }

        // this is only suitable for a container or a page, so if not, abort
        final String objectType = contentBean.getBeanType() ;
        if (!(ContainerBean.TYPE.equals(objectType) || PageBean.TYPE.equals(objectType))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not suitable for " + objectKey) ;
            }
            return null ;
        }

        // display options
        Boolean displayTimeBasedPublishing = UserPreferencesHelper.isDisplayTbpState(jParams.getUser());

        // no display required, abort
        if (!displayTimeBasedPublishing) {
            if (logger.isDebugEnabled()) {
                logger.debug("timebased publishing state display deactivated") ;
            }
            return null ;
        }

        // time based publishing status
        String tbpObjectKey = objectKey;

        // huge refactoring, what was all this mess for in the first place ??? (see tremendous HTMLToolBox.java)
        try {
            ObjectKey objKey = ObjectKey.getInstance(tbpObjectKey);
            if (ContainerBean.TYPE.equals(objectType)) {
                final ContentContainer cont = (ContentContainer) contentBean.getContentObject();
                final JahiaObjectManager jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
                JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(objKey);
                if (jahiaObjectDelegate.getRule() == null || jahiaObjectDelegate.getRule().getInherited()) {
                    final Iterator<JahiaField> en = cont.getJahiaContainer(jParams, jParams.getEntryLoadRequest()).getFields();
                    while (en.hasNext()) {
                        final JahiaField field = en.next();
                        if (field.getType() == FieldTypes.PAGE) {
                            final JahiaPage dest = (JahiaPage) ((JahiaPageField) field).getObject();
                            if (dest == null) {
                                continue;
                            }
                            int jahiaPageID = dest.getID();
                            if (jahiaPageID > 0) {
                                tbpObjectKey = PageBean.TYPE + ObjectKey.KEY_SEPARATOR + jahiaPageID;
                                break;
                            }
                        }
                    }
                }
            } else if (PageBean.TYPE.equals(objectType)) {
                ContentPage contentPage = (ContentPage) ContentPage.getContentObjectInstance(objKey);
                if (contentPage.getPageType(jParams.getEntryLoadRequest()) == JahiaPage.TYPE_LINK) {
                    int pageLinkId = contentPage.getPageLinkID(jParams);
                    if (pageLinkId > 0) {
                        ContentPage pageLink = ContentPage.getPage(pageLinkId);
                        if (pageLink != null) {
                            final JahiaObjectManager jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
                            JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(objKey);
                            if (jahiaObjectDelegate.getRule() == null || jahiaObjectDelegate.getRule().getInherited()) {
                                tbpObjectKey = pageLink.getObjectKey().toString();
                            }
                        }
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("There is a problem for " + objectKey) ;
                }
            }
        } catch (JahiaException e) {
            logger.debug("Error handling time based publishing for page link, use local rule", e);
        } catch (ClassNotFoundException e) {
            logger.debug("Error handling time based publishing for page link, use local rule", e);
        }

        // the correct object key has now been retrieved
        try {
            final TimeBasedPublishingService tbpService = ServicesRegistry.getInstance().getTimeBasedPublishingService();
            final JahiaObjectManager jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());

            // Contains params that will have to given to the ParamBean Constructor for correct
            // initialization of the latter

            ObjectKey currentObjectKey = ObjectKey.getInstance(tbpObjectKey);

            if (logger.isDebugEnabled()) {
                logger.debug("Getting Time Based Publishing State for: " + tbpObjectKey);
            }
            if (currentObjectKey != null ){
                JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(currentObjectKey);
                RetentionRule retRule = tbpService.getRetentionRule(currentObjectKey);
                boolean inherited = true;
                if (retRule == null) {
                    JahiaUser adminUser = JahiaAdminUser.getAdminUser(jahiaObjectDelegate.getSiteId());
                    currentObjectKey = tbpService.getParentObjectKeyForTimeBasedPublishing(currentObjectKey,
                            adminUser, EntryLoadRequest.STAGED, ParamBean.EDIT,
                            true);
                    if (currentObjectKey != null) {
                        jahiaObjectDelegate = jahiaObjectManager
                                .getJahiaObjectDelegate(currentObjectKey);
                        retRule = tbpService.getRetentionRule(currentObjectKey);
                    }
                } else {
                    inherited = retRule.getInherited() ;
                }

                final long now = System.currentTimeMillis();

                // retrieve the timebased publishing status
                String statusLabel = null ;
                if (retRule != null && !BaseRetentionRule.RULE_NONE.equals(retRule.getRuleType())) {
                    final boolean isValid = jahiaObjectDelegate.isValid();
                    final boolean isExpired = jahiaObjectDelegate.isExpired();
                    final boolean willExpire = jahiaObjectDelegate.willExpire(now);
                    final boolean willBecomeValid = jahiaObjectDelegate.willBecomeValid(now);
                    if (isExpired) {
                        if (willBecomeValid) {
                            statusLabel = GWTJahiaTimebasedPublishingState.WILL_BECOME_VALID ; // yellow
                        } else {
                            statusLabel = GWTJahiaTimebasedPublishingState.EXPIRED ; // red
                        }
                    } else if (isValid) {
                        if (willExpire) {
                            statusLabel = GWTJahiaTimebasedPublishingState.WILL_EXPIRE ; // orange
                        } else {
                            statusLabel = GWTJahiaTimebasedPublishingState.PUBLISHED ; // green
                        }
                    } else {
                        if (willBecomeValid) {
                            statusLabel = GWTJahiaTimebasedPublishingState.WILL_BECOME_VALID ; // yellow
                        } else {
                            // is not valid
                            statusLabel = GWTJahiaTimebasedPublishingState.INVALID ;
                        }
                    }
                    if (statusLabel.length() > 0 && inherited) {
                        statusLabel = "inherited_" + statusLabel;
                    }
                }

                // don't display any icons for objects that have no rules
                if (("inherited_"+ GWTJahiaTimebasedPublishingState.PUBLISHED).equals(statusLabel) ) {
                    statusLabel = null ;
                }

                // return a custom TimebasedPublishingState object containing all needed data
                if (statusLabel != null) {
                    return new GWTJahiaTimebasedPublishingState(statusLabel, tbpObjectKey) ;
                }
            }
        } catch (final Exception e) {
            logger.error("Unable to process the request !", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("No specific timebased publishing status found for " + objectKey) ;
        }
        return null;
    }

    /**
     * Retrieve timebased publishing details for a given content object represented by its state wrapper.
     *
     * @param jParams processing context
     * @param state the state wrapper of the content object
     * @return the details concerning this object's timebased publishing
     */
    public static GWTJahiaTimebasedPublishingDetails getTimebasedPublishingDetails(ProcessingContext jParams, GWTJahiaTimebasedPublishingState state) {
        try {
            final JahiaUser currentUser = jParams.getUser() ;
            final JahiaSite site = jParams.getSite() ;

            if (currentUser == null || site == null) {
                logger.debug("Unauthorized attempt to use GWT action menu service");
                return null ;
            }

            ObjectKey key = ObjectKey.getInstance(state.getObjectKey()) ;

            final TimeBasedPublishingService tbpService = ServicesRegistry.getInstance().getTimeBasedPublishingService();
            final JahiaObjectManager jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaObjectManager.class.getName());
            final JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager.getJahiaObjectDelegate(key);

            if (jahiaObjectDelegate == null) {
                logger.debug("Problem retrieving " + key.getKey() + " as JahiaObjectDelegate") ;
            } else {
                RetentionRule retRule = tbpService.getRetentionRule(key);

                final Locale locale = jParams.getLocale();
                String statusLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.timebBasedPublishingStatus.label", locale,"Time Based Publishing Status");
                String currentStatusLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.currentstatus.label", locale,"Current status");
                String schedulingTypeLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.schedulingType.label", locale,"Scheduling type");
                String publicationDateLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.rangerule.validFrom.label", locale,"Publication date");
                String expirationDateLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.rangerule.validTo.label", locale,"Publication date");
                String publicationDateValue = "";
                String expirationDateValue = "";
                String statusValueLabel = "";
                String schedulingType = "";

                if (retRule != null){
                    boolean inherited = retRule.getInherited() ;
                    boolean isValid = jahiaObjectDelegate.isValid();
                    boolean isExpired = jahiaObjectDelegate.isExpired();
                    schedulingType = retRule.getRuleType();
                    if (inherited) {
                        JahiaUser adminUser = JahiaAdminUser.getAdminUser(jahiaObjectDelegate.getSiteId());
                        ObjectKey contentObjectKey = tbpService.getParentObjectKeyForTimeBasedPublishing(jahiaObjectDelegate.getObjectKey(), adminUser, EntryLoadRequest.STAGED,ParamBean.EDIT,true);
                        if ( contentObjectKey != null && !contentObjectKey.equals(jahiaObjectDelegate.getObjectKey()) ){
                            final RetentionRule effectiveRetRule = tbpService.getRetentionRule(contentObjectKey);
                            schedulingType = effectiveRetRule.getRuleType();
                        }
                    }
                    int statusCode ;
                    if (isExpired) {
                        statusCode = 0;
                    } else if (isValid) {
                        statusCode = 2;
                    } else {
                        statusCode = 1;
                    }
                    statusValueLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.timebpstatus."+statusCode+".label", locale,"");
                    if (statusValueLabel == null || "".equals(statusValueLabel)) {
                        if (statusCode == 0) {
                            statusValueLabel = "expired";
                        } else if (statusCode == 2) {
                            statusValueLabel = "available";
                        } else {
                            statusValueLabel = "not available";
                        }
                    }

                    schedulingType = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.schedulingType."+schedulingType, locale,schedulingType);

                    Long dateLong ;
                    Format formater = JahiaDateFieldUtil.getDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT, locale);
                    dateLong = jahiaObjectDelegate.getValidFromDate();
                    if ( dateLong != null && dateLong != 0 ){
                        publicationDateValue = formater.format(dateLong);
                    } else {
                       publicationDateValue = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.dateNotAssigned", locale,"not assigned");
                    }
                    dateLong = jahiaObjectDelegate.getValidToDate();
                    if ( dateLong != null && dateLong != 0 ){
                        expirationDateValue = formater.format(dateLong);
                    } else {
                       expirationDateValue = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.dateNotAssigned", locale,"not assigned");
                    }
                }
                String objectKey = state.getObjectKey() ;
                ContentObject obj = JahiaObjectCreator.getContentObjectFromKey(key) ;
                String url ;
                if (objectKey.startsWith(ContentContainerListKey.CONTAINERLIST_TYPE)) {
                    url = ActionMenuServiceHelper.drawContainerListPropertiesLauncher(jParams, (ContentContainerList) obj, false, 0, "timeBasedPublishing") ;
                } else if (objectKey.startsWith(ContentContainerKey.CONTAINER_TYPE)) {
                    url = ActionMenuServiceHelper.drawUpdateContainerLauncher(jParams, (ContentContainer) obj, false, 0, "timeBasedPublishing") ;
                } else if (objectKey.startsWith(ContentPageKey.PAGE_TYPE)) {
                    url = ActionMenuServiceHelper.drawPagePropertiesLauncher(jParams, false, key.getIdInType(), "timeBasedPublishing") ;
                } else {
                    url = null ;
                }

                return new GWTJahiaTimebasedPublishingDetails(statusLabel,
                                                      currentStatusLabel,
                                                      schedulingTypeLabel,
                                                      publicationDateLabel,
                                                      expirationDateLabel,
                                                      publicationDateValue,
                                                      expirationDateValue,
                                                      statusValueLabel,
                                                      schedulingType,
                                                      url) ;
            }
        } catch (final Exception e) {
            logger.error("error", e);
        }
        return null ;
    }

}
