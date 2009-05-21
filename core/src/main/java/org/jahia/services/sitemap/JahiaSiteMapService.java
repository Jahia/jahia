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
package org.jahia.services.sitemap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinitionProperties;
import org.jahia.data.viewhelper.sitemap.FlatSiteMapViewHelper;
import org.jahia.data.viewhelper.sitemap.LimitedTemplatesFilter;
import org.jahia.data.viewhelper.sitemap.PagesFilter;
import org.jahia.data.viewhelper.sitemap.SiteMapViewHelper;
import org.jahia.data.viewhelper.sitemap.TreeSiteMapViewHelper;
import org.jahia.data.viewhelper.sitemap.WorkflowSiteMapViewHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * This class create and manage a site map helper that can be imported to a
 * view file such as JSP.
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 */
public class JahiaSiteMapService extends JahiaService {
    /**
     * The pages filter parameter 
     */
    public static final String PAGES_FILTER = "pagesFilter";     
    public static final String FILTER_INIT_PARAMETER = "filterInitParameter";
    
    /**
     * Returns the singleton instance of the service, and creates it if there
     * wasn't one.
     * @return a JahiaSiteMapService object that is the singleton instance of
     * this service.
     */
    public static synchronized JahiaSiteMapService getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new JahiaSiteMapService();
        }
        return singletonInstance;
    }

    /**
     * Return the pagesFilter instance set for this request in the parameters.
     *
     * @param jParams Jahia processing context
     */
    public static PagesFilter getCurrentPagesFilter(JahiaField theField, ProcessingContext jParams) throws JahiaException {
        PagesFilter pagesFilter = null;

        if (jParams != null) {
            final String pagesFilterClassName = (theField != null ? theField
                    .getDefinition()
                    .getProperty(
                            JahiaFieldDefinitionProperties.PAGE_SELECTION_FILTER_PROP)
                    : jParams.getParameter(JahiaSiteMapService.PAGES_FILTER));
            final JahiaPageBaseService jahiaPageBaseService = JahiaPageBaseService
                    .getInstance();
            String fieldDefValue = "";
            if (theField != null) {
                ContentPage contentPage = jahiaPageBaseService
                        .lookupContentPage(theField.getPageID(), false);
                if (contentPage != null) {
                    fieldDefValue = theField.getDefinition().getDefaultValue(
                    );
                }
            } else {
                fieldDefValue = jParams
                        .getParameter(JahiaSiteMapService.FILTER_INIT_PARAMETER);
            }

            if (pagesFilterClassName != null
                    && pagesFilterClassName.length() > 0) {
                try {
                    pagesFilter = (PagesFilter) Class.forName(
                            pagesFilterClassName).newInstance();
                    pagesFilter.setFieldDefaultValue(fieldDefValue);
                } catch (Exception e) {
                    logger.warn("Cannot instantiate pages filter!", e);
                    pagesFilter = new LimitedTemplatesFilter();
                    pagesFilter.setFieldDefaultValue(fieldDefValue);                    
                }
            } else if (fieldDefValue != null && fieldDefValue.length() > 0) {
                pagesFilter = new LimitedTemplatesFilter();
                pagesFilter.setFieldDefaultValue(fieldDefValue);
            }
        }
        return (pagesFilter);
    }       
    
    
    
    public void start() {
        logger.debug("Start Jahia Site map Service");
        Class siteMapViewHelperClass = TreeSiteMapViewHelper.class;
        Method[] siteMapViewHelperClassMethods = siteMapViewHelperClass.getMethods();
        for (int i = 0; i < siteMapViewHelperClassMethods.length; i++) {
            Method aSiteMapViewHelperMethod = siteMapViewHelperClassMethods[i];
            Class[] paramTypes = aSiteMapViewHelperMethod.getParameterTypes();
            Class returnType = aSiteMapViewHelperMethod.getReturnType();
            // Filter the methods having one param of type int returning void.
            if (paramTypes.length == 1 && paramTypes[0] == int.class && returnType == void.class) {
                _siteMapMethods.put(aSiteMapViewHelperMethod.getName(), aSiteMapViewHelperMethod);
            }
        }
        // Try to get the 'sortSiteMap' method
        Class[] cl = { String.class, int.class, int.class };
        Class flatSiteMapViewHelperClass = FlatSiteMapViewHelper.class;
        try {
            Method sortSiteMapMethod = flatSiteMapViewHelperClass.getMethod("sortSiteMap", cl);
            _siteMapMethods.put(sortSiteMapMethod.getName(), sortSiteMapMethod);
        } catch (NoSuchMethodException nsme) {logger.debug(nsme, nsme);}
        logger.debug(_siteMapMethods.toString());
    }

    public void stop() {
    }

    /**
     * Construct the tree site map view helper if necessary. In the case a same user
     * accesses from a same page to the site map, a previous stored site map view
     * helper is returned. Otherwise a new one is created.
     * The reason of page dependency is to guarantee a low memory obstruction.
     *
     * @param user The Jahia user context referencing the site map.
     * @param page The Jahia page context referencing the site map.
     * @param sessionID The session ID defines another contextual key if the same
     * user is logged more than one time.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param languageCode Get the page with the specified language code.
     * @param defaultMaxLevel Maximum page level that should be displayed by default.
     * @param pagesFilter     Filter object for page filtering  
     * @return The site map view helper.
     */
    public SiteMapViewHelper getTreeSiteMapViewHelper(JahiaUser user, ContentPage page,
            String sessionID, int pageInfosFlag, String languageCode, int defaultMaxLevel, 
            boolean directPageOnly, PagesFilter pagesFilter) {
        List pages = new ArrayList();
        pages.add(page);
        return getTreeSiteMapViewHelper(user,pages,sessionID,pageInfosFlag,languageCode,defaultMaxLevel,directPageOnly,pagesFilter);
    }
    
    public SiteMapViewHelper getTreeSiteMapViewHelper(JahiaUser user, List pages,
            String sessionID, int pageInfosFlag, String languageCode, int defaultMaxLevel, 
            boolean directPageOnly, PagesFilter pagesFilter) {        
        String pagesFilterClassName = (pagesFilter != null ? pagesFilter.getClass().getName() : null);
        StringBuffer pageIDBuffer = new StringBuffer();
        for (Iterator i = pages.iterator(); i.hasNext();) {
            pageIDBuffer.append(((ContentPage)i.next()).getID()).append(":");
        }
        String pageIDs = pageIDBuffer.toString();
        String context = user.getUserKey() + ":" + sessionID + ":" + pageIDs +
                         pageInfosFlag + ":" + languageCode + 
                         (pagesFilterClassName == null ? "" : ":" + pagesFilterClassName);
        SiteMapViewHelper siteMapViewHelper =
                (SiteMapViewHelper)_treeSiteMapViewHelper.get(context);
        if (siteMapViewHelper == null) {
            String message = "Rebuild site map view helper for user '" + user.getUserKey() +
                             "', page ID '" + pageIDs + "', page infos flag '" +
                             pageInfosFlag + "' and language code '" + languageCode + "'";
            logger.debug(message);
            siteMapViewHelper = new TreeSiteMapViewHelper(user, pages, pageInfosFlag, languageCode, defaultMaxLevel, directPageOnly, pagesFilter, true);
            _treeSiteMapViewHelper.put(context, siteMapViewHelper);
        }
        return siteMapViewHelper;
    }
   /**
     * Construct the tree site map view helper if necessary. In the case a same user
     * accesses from a same page to the site map, a previous stored site map view
     * helper is returned. Otherwise a new one is created.
     * The reason of page dependency is to guarantee a low memory obstruction.
     *
     * @param user The Jahia user context referencing the site map.
     * @param page The Jahia page context referencing the site map.
     * @param sessionID The session ID defines another contextual key if the same
     * user is logged more than one time.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param languageCode Get the page with the specified language code.
     * @param defaultMaxLevel Maximum page level that should be displayed by default.
     * @param pagesFilter     Filter object for page filtering 
     * @return The site map view helper.
     */
    public SiteMapViewHelper getWorkflowSiteMapViewHelper(JahiaUser user, ContentPage page,
            String sessionID, int pageInfosFlag, String languageCode, int defaultMaxLevel, PagesFilter pagesFilter) {
        String pagesFilterClassName = (pagesFilter != null ? pagesFilter.getClass().getName() : null);
        String context = user.getUserKey() + ":" + sessionID + ":" + page.getID() + ":" +
                         pageInfosFlag + ":" + languageCode + 
                         (pagesFilterClassName == null ? "" : ":" + pagesFilterClassName);
        SiteMapViewHelper siteMapViewHelper =
                (SiteMapViewHelper)_workflowSiteMapViewHelper.get(context);
        if (siteMapViewHelper == null) {
            String message = "Rebuild site map view helper for user '" + user.getUserKey() +
                             "', page ID '" + page.getID() + "', page infos flag '" +
                             pageInfosFlag + "' and language code '" + languageCode + "'";
            logger.debug(message);
            siteMapViewHelper = new WorkflowSiteMapViewHelper(user, page, pageInfosFlag, languageCode, defaultMaxLevel, pagesFilter);
            _workflowSiteMapViewHelper.put(context, siteMapViewHelper);
        }
        return siteMapViewHelper;
    }

    /**
     * Construct the tree site map view helper if necessary. In the case a same user
     * accesses from a same page to the site map, a previous stored site map view
     * helper is returned. Otherwise a new one is created.
     * The reason of page dependency is to guarantee a low memory obstruction.
     *
     * @param user The Jahia user context referencing the site map.
     * @param page The Jahia page context referencing the site map.
     * @param sessionID The session ID defines another contextual key if the same
     * user is logged more than one time.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param defaultMaxLevel Maximum page level that should be displayed by default.
     * @param languageCode Get the page with the specified language code.
     * @param pagesFilter     Filter object for page filtering      
     * @return The site map view helper.
     */
    public SiteMapViewHelper getTreeSiteMapViewHelper(JahiaUser user, ContentPage page,
            String sessionID, int pageInfosFlag, String languageCode, int defaultMaxLevel) {
        return getTreeSiteMapViewHelper(user,page,sessionID,pageInfosFlag,languageCode,defaultMaxLevel,true,null);
    }

    /**
     * Construct the flat site map view helper if necessary. In the case a same user
     * accesses from a same page to the site map, a previous stored site map view
     * helper is returned. Otherwise a new one is created.
     * The reason of page dependency is to guarantee a low memory obstruction.
     *
     * @param user The Jahia user context referencing the site map.
     * @param page The Jahia page context referencing the site map.
     * @param sessionID The session ID defines another contextual key if the same
     * user is logged more than one time.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param languageCode Get the page with the specified language code.
     * @param defaultMaxLevel Maximum page level that should be displayed by default.
     * 
     * @return The site map view helper.
     */
    public SiteMapViewHelper getFlatSiteMapViewHelper(ProcessingContext jParams, JahiaUser user, ContentPage page,
            String sessionID, int pageInfosFlag, String languageCode, int defaultMaxLevel, PagesFilter pagesFilter) {
        String pagesFilterClassName = (pagesFilter != null ? pagesFilter.getClass().getName() : null);          
        String context = user.getUserKey() + ":" + sessionID + ":" + page.getID() + ":" +
                         pageInfosFlag + ":" + languageCode + 
                         (pagesFilterClassName == null ? "" : ":" + pagesFilterClassName);
        SiteMapViewHelper siteMapViewHelper =
                (SiteMapViewHelper)_flatSiteMapViewHelper.get(context);
        if (siteMapViewHelper == null) {
            String message = "Rebuild site map view helper for user '" + user.getUserKey() +
                             "', page ID '" + page.getID() + "', page infos flag '" +
                             pageInfosFlag + "' and language code '" + languageCode + "'";
            logger.debug(message);
            siteMapViewHelper = new FlatSiteMapViewHelper(jParams, user, page, pageInfosFlag, languageCode, defaultMaxLevel, pagesFilter);
            _flatSiteMapViewHelper.put(context, siteMapViewHelper);
        }
        return siteMapViewHelper;
    }

    /**
     * Invalid the site map view helper that should be reinstanciate due
     * to a Jahia page addition.
     * Called from org.jahia.data.viewhelper.sitemap.SiteMapEventListener
     */
    public void resetSiteMap() {
        logger.debug("Reset site map view helper");
        _treeSiteMapViewHelper.clear();
        _flatSiteMapViewHelper.clear();
        _workflowSiteMapViewHelper.clear();
    }

    /**
     * Remove the sitemap corresponding to the Jahia user.
     * Called from org.jahia.data.viewhelper.sitemap.SiteMapEventListener
     *
     * @param userKey The user object to remove from hash map.
     */
    public void removeUserSiteMap(String userKey) {
        logger.debug("Remove user " + userKey + " from site map view helper");
        Set contextKeys = _treeSiteMapViewHelper.keySet();
        Iterator it = contextKeys.iterator();
        while (it.hasNext()) {
            String context = (String)it.next();
            if (context.indexOf(userKey) != -1) {
                _treeSiteMapViewHelper.remove(context);
                // Reinit the iterator to avoid Concurrent Modification Exception.
                contextKeys = _treeSiteMapViewHelper.keySet();
                it = contextKeys.iterator();
            }
        }
        contextKeys = _flatSiteMapViewHelper.keySet();
        it = contextKeys.iterator();
        while (it.hasNext()) {
            String context = (String)it.next();
            if (context.indexOf(userKey) != -1) {
                _flatSiteMapViewHelper.remove(context);
                // Reinit the iterator to avoid Concurrent Modification Exception.
                contextKeys = _flatSiteMapViewHelper.keySet();
                it = contextKeys.iterator();
            }
        }
        contextKeys = _workflowSiteMapViewHelper.keySet();
        it = contextKeys.iterator();
        while (it.hasNext()) {
            String context = (String)it.next();
            if (context.indexOf(userKey) != -1) {
                _workflowSiteMapViewHelper.remove(context);
                // Reinit the iterator to avoid Concurrent Modification Exception.
                contextKeys = _workflowSiteMapViewHelper.keySet();
                it = contextKeys.iterator();
            }
        }
    }

    /**
     * Remove the sitemap corresponding to the Jahia user.
     * Called from org.jahia.data.viewhelper.sitemap.SiteMapEventListener
     *
     */
    public void removeSessionSiteMap(String sessionID) {
        logger.debug("Remove session " + sessionID + " from site map view helper");
        String sessionPart = ":" + sessionID + ":";
        Set contextKeys = _treeSiteMapViewHelper.keySet();
        Iterator it = contextKeys.iterator();
        while (it.hasNext()) {
            String context = (String)it.next();
            if (context.indexOf(sessionPart) != -1) {
                _treeSiteMapViewHelper.remove(context);
                // Reinit the iterator to avoid Concurrent Modification Exception.
                contextKeys = _treeSiteMapViewHelper.keySet();
                it = contextKeys.iterator();
            }
        }
        contextKeys = _flatSiteMapViewHelper.keySet();
        it = contextKeys.iterator();
        while (it.hasNext()) {
            String context = (String)it.next();
            if (context.indexOf(sessionPart) != -1) {
                _flatSiteMapViewHelper.remove(context);
                // Reinit the iterator to avoid Concurrent Modification Exception.
                contextKeys = _flatSiteMapViewHelper.keySet();
                it = contextKeys.iterator();
            }
        }
        contextKeys = _workflowSiteMapViewHelper.keySet();
        it = contextKeys.iterator();
        while (it.hasNext()) {
            String context = (String)it.next();
            if (context.indexOf(sessionPart) != -1) {
                _workflowSiteMapViewHelper.remove(context);
                // Reinit the iterator to avoid Concurrent Modification Exception.
                contextKeys = _workflowSiteMapViewHelper.keySet();
                it = contextKeys.iterator();
            }
        }
    }

    /**
     * Call the appropriate tree site map view helper method in a specified context.
     * @param user The logged user who use the site map.
     * @param page The page from where the site map is used.
     * @param sessionID The session ID defines another contextual key if the same
     * user is logged more than one time.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param languageCode Get the page with the specified language code.
     * @param siteMapParam The site map method parameters.
     */
    public void invokeTreeSiteMapViewHelperMethod(JahiaUser user, ContentPage page,
            String sessionID, int pageInfosFlag, String languageCode, String siteMapParam, 
            boolean directPageOnly, PagesFilter pagesFilter, ProcessingContext jParams) {
        List pages = new ArrayList();
        pages.add(page);
        invokeTreeSiteMapViewHelperMethod(user, pages, sessionID, pageInfosFlag, 
                languageCode, siteMapParam, directPageOnly, pagesFilter, jParams);                
    }
    
    public void invokeTreeSiteMapViewHelperMethod(JahiaUser user, List pages,
            String sessionID, int pageInfosFlag, String languageCode, String siteMapParam, 
            boolean directPageOnly, PagesFilter pagesFilter, ProcessingContext jParams) {    
        StringTokenizer siteMapParamTokens = new StringTokenizer(siteMapParam, "|");
        String methodName = (String)_urlParams.get(siteMapParamTokens.nextToken());
        Method method = (Method)_siteMapMethods.get(methodName);
        SiteMapViewHelper siteMapViewHelper = (jParams != null) ? 
                getTreeSiteMapViewHelper(user, pages,
                      sessionID, pageInfosFlag, languageCode, TreeSiteMapViewHelper.DISPLAY_ALL_LEVEL, directPageOnly, pagesFilter) : 
                getTreeSiteMapViewHelper(user, pages,
                      sessionID, pageInfosFlag, languageCode, TreeSiteMapViewHelper.DISPLAY_ALL_LEVEL, directPageOnly, null);        
        try {
            if (siteMapParamTokens.countTokens() == 1) {
                Object[] args = { new Integer(siteMapParamTokens.nextToken()) };
                method.invoke(siteMapViewHelper, args);
            }
        } catch(InvocationTargetException ite) {
            logger.debug("Cannot invoke method " + methodName, ite);
        } catch(IllegalAccessException iae) {
            logger.debug("Bad access to method " + methodName, iae);
        }
    }

    /**
     * Call the appropriate tree site map view helper method in a specified context.
     * @param user The logged user who use the site map.
     * @param page The page from where the site map is used.
     * @param sessionID The session ID defines another contextual key if the same
     * user is logged more than one time.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param languageCode Get the page with the specified language code.
     * @param siteMapParam The site map method parameters.
     */
    public void invokeWorkflowSiteMapViewHelperMethod(JahiaUser user, ContentPage page,
            String sessionID, int pageInfosFlag, String languageCode, String siteMapParam, 
            PagesFilter pagesFilter) {
        StringTokenizer siteMapParamTokens = new StringTokenizer(siteMapParam, "|");
        String methodName = (String)_urlParams.get(siteMapParamTokens.nextToken());
        Method method = (Method)_siteMapMethods.get(methodName);
        SiteMapViewHelper siteMapViewHelper = getWorkflowSiteMapViewHelper(user, page,
                sessionID, pageInfosFlag, languageCode, TreeSiteMapViewHelper.DISPLAY_ALL_LEVEL, 
                pagesFilter);
        try {
            if (siteMapParamTokens.countTokens() == 1) {
                Object[] args = { new Integer(siteMapParamTokens.nextToken()) };
                method.invoke(siteMapViewHelper, args);
            }
        } catch(InvocationTargetException ite) {
            logger.debug("Cannot invoke method " + methodName, ite);
        } catch(IllegalAccessException iae) {
            logger.debug("Bad access to method " + methodName, iae);
        }
    }

    /**
     * Call the appropriate flat site map view helper method in a specified context.
     * @param user The logged user who use the site map.
     * @param page The page from where the site map is used.
     * @param sessionID The session ID defines another contextual key if the same
     * user is logged more than one time.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param languageCode Get the page with the specified language code.
     * @param siteMapParam The site map method parameters.
     */
    public void invokeFlatSiteMapViewHelperMethod(ProcessingContext jParams, JahiaUser user, ContentPage page,
            String sessionID, int pageInfosFlag, String languageCode, String siteMapParam, PagesFilter pagesFilter) {
        StringTokenizer siteMapParamTokens = new StringTokenizer(siteMapParam, "|");
        String methodName = (String)_urlParams.get(siteMapParamTokens.nextToken());
        Method method = (Method)_siteMapMethods.get(methodName);
        SiteMapViewHelper siteMapViewHelper = getFlatSiteMapViewHelper(jParams, user, page,
                sessionID, pageInfosFlag, languageCode, SiteMapViewHelper.DISPLAY_ALL_LEVEL, pagesFilter);
        try {
            if (siteMapParamTokens.countTokens() == 3) {
                Object[] args = { siteMapParamTokens.nextToken(),
                                  new Integer(siteMapParamTokens.nextToken()),
                                  new Integer(siteMapParamTokens.nextToken()) };
                method.invoke(siteMapViewHelper, args);
            }
        } catch(InvocationTargetException ite) {
            logger.debug("Cannot invoke method " + methodName, ite);
        } catch(IllegalAccessException iae) {
            logger.debug("Bad access to method " + methodName, iae);
        }
    }

    protected JahiaSiteMapService() {
        _urlParams.put("collapse", "collapsePagesSubTree");
        _urlParams.put("expandall", "expandAllPagesSubTree");
        _urlParams.put("expand", "expandPagesSubTree");
        _urlParams.put("hideerror", "hideErrors");
        _urlParams.put("hideevent", "hideEvents");
        _urlParams.put("hideinfo", "hideInformation");
        _urlParams.put("hidewarning", "hideWarnings");
        _urlParams.put("showerror", "showErrors");
        _urlParams.put("showevent", "showEvents");
        _urlParams.put("showinfo", "showInformation");
        _urlParams.put("showwarning", "showWarnings");
        _urlParams.put("sort", "sortSiteMap");
    }

    static private JahiaSiteMapService singletonInstance = null;
    // Store the tree site map view helper for a given context.
    private Map _treeSiteMapViewHelper = new HashMap();
    // Store the tree site map view helper for a given context.
    private Map _workflowSiteMapViewHelper = new HashMap();
    // Store the flat site map view helper for a given context.
    private Map _flatSiteMapViewHelper = new HashMap();
    // Store the parameters key corresponding to method pointers
    private Map _siteMapMethods = new HashMap();
    // To avoid a long URL parameter this hash map store the match between the
    // site map view helper method to call and a parameter.
    private Map _urlParams = new HashMap();

    private static Logger logger = Logger.getLogger(JahiaSiteMapService.class);
    
    
    /**
     * Remove page information from session site map 
     * 
     * @param user
     * @param page
     * @param sessionID
     */
    public void removeSessionPageSiteMap(JahiaUser user, ContentPage page,
      String sessionID)
    {      
      logger.debug("Remove session " + sessionID + " page from site map view helper");
      String contextPart = user.getUserKey() + ":" + sessionID + ":" + page.getID() + ":";
      Set contextKeys = _treeSiteMapViewHelper.keySet();
      Iterator it = contextKeys.iterator();
      while (it.hasNext()) {
          String context = (String)it.next();
          if (context.indexOf(contextPart) != -1) {
              _treeSiteMapViewHelper.remove(context);
              // Reinit the iterator to avoid Concurrent Modification Exception.
              contextKeys = _treeSiteMapViewHelper.keySet();
              it = contextKeys.iterator();
          }
      }
      contextKeys = _flatSiteMapViewHelper.keySet();
      it = contextKeys.iterator();
      while (it.hasNext()) {
          String context = (String)it.next();
          if (context.indexOf(contextPart) != -1) {
              _flatSiteMapViewHelper.remove(context);
              // Reinit the iterator to avoid Concurrent Modification Exception.
              contextKeys = _flatSiteMapViewHelper.keySet();
              it = contextKeys.iterator();
          }
      }
    }  
}
