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

package org.jahia.data.viewhelper.sitemap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.PageInfoInterface;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.comparator.JahiaPageComparator;

/**
 * <p>The Jahia Shared Modification is: Jahia View Helper</p>
 * <p/>
 * <p>Description:
 * Create a flat tree from Jahia page tree structure destinated to display a tree
 * or a flat site map.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 *
 * @author MAP
 * @version 1.0
 */
public abstract class SiteMapViewHelper implements Serializable {

    public static final int DISPLAY_ALL_LEVEL = Integer.MAX_VALUE;
    public static final int DEFAULT_LEVEL = 2;

    public static final int TREE_VIEW = 0;
    public static final int FLAT_VIEW = 1;
    public static final int SEARCH_VIEW = 2;    
    
    private JahiaObjectManager jahiaObjectManager = null;
    private JahiaPageService pageBaseService = null;

    /**
     * Create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user            The actual user logged in Jahia.
     * @param startPage       The start page for site map.
     * @param pageInfosFlag   Kind of page infos desired. This parameter can associate
     * @param languageCode    Get the page with the specified language code.
     * @param defaultMaxLevel Site map expansion default level max.
     *                        the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     *                        ARCHIVED_PAGE_INFOS constants.
     */
    protected SiteMapViewHelper(JahiaUser user,
                                ContentPage startPage,
                                int pageInfosFlag,
                                String languageCode,
                                int defaultMaxLevel,
                                PagesFilter pagesFilter, boolean doLoad) {
        initialize(user, startPage, pageInfosFlag, languageCode, 
                defaultMaxLevel, true, pagesFilter, doLoad);            
    }

    /**
     * Create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user            The actual user logged in Jahia.
     * @param startPage       The start page for site map.
     * @param pageInfosFlag   Kind of page infos desired. This parameter can associate
     * @param languageCode    Get the page with the specified language code.
     * @param defaultMaxLevel Site map expansion default level max.
     * @param directPageOnly  the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     *                        ARCHIVED_PAGE_INFOS constants.
     * @param pagesFilter     Filter object for page filtering                                                
     */
    protected SiteMapViewHelper(JahiaUser user,
                                ContentPage startPage,
                                int pageInfosFlag,
                                String languageCode,
                                int defaultMaxLevel,
                                boolean directPageOnly,
                                PagesFilter pagesFilter, boolean doLoad) {
        initialize(user, startPage, pageInfosFlag, languageCode, 
                defaultMaxLevel, directPageOnly, pagesFilter, doLoad);            
    }
    
    /**
     * Create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user            The actual user logged in Jahia.
     * @param startPages      A list of start pages for site map.
     * @param pageInfosFlag   Kind of page infos desired. This parameter can associate
     * @param languageCode    Get the page with the specified language code.
     * @param defaultMaxLevel Site map expansion default level max.
     * @param directPageOnly  the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     *                        ARCHIVED_PAGE_INFOS constants.
     * @param pagesFilter     Filter object for page filtering                        
     */    
    public SiteMapViewHelper(JahiaUser user, List startPages,
            int pageInfosFlag, String languageCode, int defaultMaxLevel,
            boolean directPagesOnly, PagesFilter pagesFilter, boolean doLoad) {
        initialize(user, startPages, pageInfosFlag, languageCode,
                defaultMaxLevel, directPagesOnly, pagesFilter, doLoad);
    }       
    
    /**
     * Initialize and create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user       The actual user logged in Jahia.
     * @param startPage  The start page for site map.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * @param languageCode Get the page with the specified language code. 
     * @param defaultMaxLevel Site map expansion default level max.
     * @param directPageOnly
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param pagesFilter     Filter object for page filtering 
     */
    private void initialize (JahiaUser user, ContentPage startPage,
            int pageInfosFlag, String languageCode,
            int defaultMaxLevel, boolean directPageOnly, 
            PagesFilter pagesFilter, boolean doLoad){
        List startPages = new ArrayList();
        startPages.add(startPage);        
        initialize(user, startPages, pageInfosFlag, languageCode,
                defaultMaxLevel, directPageOnly, pagesFilter, doLoad);        
    }
    
    /**
     * Initialize and create a view helper on a entire Jahia site map restricted to the actual
     * logged user.
     *
     * @param user       The actual user logged in Jahia.
     * @param startPages The start pages for site map.
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     * @param languageCode Get the page with the specified language code. 
     * @param defaultMaxLevel Site map expansion default level max.
     * @param directPageOnly
     * the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     * ARCHIVED_PAGE_INFOS constants.
     * @param pagesFilter     Filter object for page filtering 
     */        
    private void initialize(JahiaUser user, List startPages,
            int pageInfosFlag, String languageCode, int defaultMaxLevel,
            boolean directPageOnly, PagesFilter pagesFilter, boolean doLoad) {
        jahiaObjectManager = (JahiaObjectManager) SpringContextSingleton
                .getInstance().getContext().getBean(
                        JahiaObjectManager.class.getName());
        pageBaseService = ServicesRegistry.getInstance().getJahiaPageService();
        _user = user;
        _pageInfosFlag = pageInfosFlag;
        _languageCode = languageCode;
        _maxLevel = 0;
        _defaultMaxLevel = defaultMaxLevel;
        _pagesFilter = pagesFilter;        
        _directPageOnly = directPageOnly;  
        _loadRequest = createEntryLoadRequest(pageInfosFlag, languageCode);
        
        if (doLoad) {
            ProcessingContext context = Jahia.getThreadParamBean();
            for (Iterator pageIterator = startPages.iterator(); pageIterator
                    .hasNext();) {
                final ContentPage processedPage = (ContentPage) pageIterator.next();
                List childPages = getPageChilds(processedPage);

                int pageID = processedPage.getID();
                ContentPage contentPage = lookupContentPage(pageID);

                if (_pagesFilter != null)
                    childPages = _pagesFilter.filterChildren(contentPage,
                            childPages, context);

                if (contentPage != null) {
                    if (_pagesFilter == null
                            || !_pagesFilter.filterForDisplay(contentPage,
                                    context)) {
                        if (contentPage.getPageType(_loadRequest) == PageInfoInterface.TYPE_LINK) {
                            contentPage = lookupContentPage(contentPage
                                    .getPageLinkID(_loadRequest));
                            pageID = contentPage.getID();
                        }
                        // The parent page parameter is set to 0 to avoid
                        // transmitting the ParamBean
                        _jahiaPageSiteMap
                                .add(new PageSiteMap(
                                        pageID,
                                        _currentLevel,
                                        childPages.size() > 0,
                                        0,
                                        false,
                                        contentPage
                                                .getTitles(ContentPage.LAST_UPDATED_TITLES),
                                        0, _defaultMaxLevel));
                    }
                }

                getAllPageChilds(processedPage, childPages.iterator(), context);
            }
        }
    }

    /**
     * Return the maximum page level computed in the 'getAllPageChilds' method
     * called when this object is created.
     *
     * @return The maximum level.
     */
    public int getMaxLevel() {
        return _maxLevel;
    }

    /**
     * Verify if a language is available in a specified workflow state. This
     * method is usefull if we should display exactly the number of language
     * columns in a site map. For example, a anonymous user (guest) does not
     * need to know all languages defined in the site but in which languages
     * (exactly) the active pages are available.
     *
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     *                      the ContentPage ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS and
     *                      ARCHIVED_PAGE_INFOS constants.
     * @param languageCode  Get the page with the specified language code.
     * @return True if the language code is available in the specified workflow
     *         state.
     */
    public boolean isAvailableLanguageCode(int pageInfosFlag, String languageCode) {
        Map availableLanguageCode = (Map) _availableLanguageCodeMap.get(new Integer(pageInfosFlag));
        if (availableLanguageCode == null) {
            availableLanguageCode = new HashMap();
        }
        // If already exists for this page infos, don't recompute it.
        if (!availableLanguageCode.containsKey(languageCode)) {
            int i; // Unfortunately we have to look for all pages...
            for (i = 0; i < _jahiaPageSiteMap.size(); i++) {
                final PageSiteMap pageSiteMap = (PageSiteMap) _jahiaPageSiteMap.get(i);
                final ContentPage contentPage = lookupContentPage(pageSiteMap.getPageID());
                if (contentPage != null && contentPage.hasEntries(pageInfosFlag, languageCode)) {
                    availableLanguageCode.put(languageCode, "yes");
                    break; // ... but when find it, it's sufficiant to stop the loop.
                }
            }
            // If not found, don't reiterate a second time this loop for this language
            if (i == _jahiaPageSiteMap.size()) {
                availableLanguageCode.put(languageCode, "no");
            }
        }
        _availableLanguageCodeMap.put(new Integer(pageInfosFlag), availableLanguageCode);
        return "yes".equals(availableLanguageCode.get(languageCode));
    }

    public abstract ContentPage getContentPage(int index);

    public abstract int getPageID(int index);

    public abstract int getPageIndex(int pageID);

    public abstract int getParentPageID(int index);

    public abstract int getPageLevel(int index);

    public abstract String getPageTitle(int index, String languageCode);

    public abstract int size();

    protected ContentPage lookupContentPage(int pageID) {
        try {
            return pageBaseService.lookupContentPage(pageID, false);
        } catch (JahiaException je) {
            logger.debug("Cannot recover the page with ID " + pageID);
            return null;
        }
    }

    protected List getJahiaPageSiteMap() {
        return _jahiaPageSiteMap;
    }

    protected void getAllPageChilds(ContentObject nodePage, Iterator childPageEnum, ProcessingContext context) {
        _currentLevel++;
        if (_currentLevel > _maxLevel) {
            _maxLevel = _currentLevel;
        }
        while (childPageEnum.hasNext()) {
            final ContentObject childObject = (ContentObject) childPageEnum.next();
            List nextChildPages = getPageChilds(childObject);

            if (childObject instanceof ContentPage) {
                final int pageID = childObject.getID();
                final ContentPage contentPage = (ContentPage) childObject;
                // Stop loop if page has a deleted entry only.
                if (this._pageInfosFlag < ContentPage.ARCHIVED_PAGE_INFOS
                        && contentPage.hasEntries(ContentPage.ARCHIVED_PAGE_INFOS) &&
                        !(contentPage.hasEntries(ContentPage.ACTIVE_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS))) {
                    break;
                }
                
                if (_pagesFilter != null) 
                    nextChildPages = _pagesFilter.filterChildren(contentPage, nextChildPages, context);                                

                int sameParentID = contentPage.hasSameParentID();
                if (((_pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0) ||
                        ((_pageInfosFlag & ContentPage.ARCHIVED_PAGE_INFOS) != 0)) {
                    // we want staged site map or versioned
                    if ((sameParentID == ContentPage.SAME_PARENT)) {
                        if (_pagesFilter == null
                                || !_pagesFilter.filterForDisplay(contentPage,
                                        context)) {
                            final PageSiteMap pageSiteMap = new PageSiteMap(
                                    pageID,
                                    _currentLevel,
                                    nextChildPages.size() > 0,
                                    nodePage.getID(),
                                    !childPageEnum.hasNext(),
                                    contentPage
                                            .getTitles(ContentPage.LAST_UPDATED_TITLES),
                                    _currentLevel, _defaultMaxLevel);
                            _jahiaPageSiteMap.add(pageSiteMap);
                        }
                        getAllPageChilds(childObject, nextChildPages.iterator(), context);
                    } else {
                        // we want to get the staging parent and if it is equals to the current node page, then we continue, else
                        // we stop expanding the tree
                        final int parentId = contentPage.getParentID(EntryLoadRequest.STAGED);
                        if (parentId == nodePage.getID()) {
                            if (_pagesFilter == null
                                    || !_pagesFilter.filterForDisplay(
                                            contentPage, context)) {
                                final PageSiteMap pageSiteMap = new PageSiteMap(
                                        pageID,
                                        _currentLevel,
                                        nextChildPages.size() > 0,
                                        nodePage.getID(),
                                        !childPageEnum.hasNext(),
                                        contentPage
                                                .getTitles(ContentPage.LAST_UPDATED_TITLES),
                                        _currentLevel, _defaultMaxLevel);
                                _jahiaPageSiteMap.add(pageSiteMap);
                            }
                            getAllPageChilds(childObject, nextChildPages.iterator(), context);
                        } else if (_pagesFilter == null || !_pagesFilter.filterForDisplay(contentPage, context)){
                            final PageSiteMap pageSiteMap = new PageSiteMap(pageID,
                                    _currentLevel,
                                    false, nodePage.getID(),
                                    !childPageEnum.hasNext(),
                                    contentPage.
                                            getTitles(ContentPage.LAST_UPDATED_TITLES),
                                    _currentLevel, _defaultMaxLevel);
                            _jahiaPageSiteMap.add(pageSiteMap);
                        }
                    }
                } else {
                    // we want active site map
                    if ((sameParentID == ContentPage.SAME_PARENT) ||
                            sameParentID == nodePage.getID()) {
                        if (_pagesFilter == null
                                || !_pagesFilter.filterForDisplay(contentPage,
                                        context)) {
                            final PageSiteMap pageSiteMap = new PageSiteMap(
                                    pageID,
                                    _currentLevel,
                                    nextChildPages.size() > 0,
                                    nodePage.getID(),
                                    !childPageEnum.hasNext(),
                                    contentPage
                                            .getTitles(ContentPage.LAST_UPDATED_TITLES),
                                    _currentLevel, _defaultMaxLevel);
                            _jahiaPageSiteMap.add(pageSiteMap);
                        }
                        getAllPageChilds(childObject, nextChildPages.iterator(), context);
                    } else if (_pagesFilter == null || !_pagesFilter.filterForDisplay(contentPage, context)) { 
                        // It is the active page, break down tree construction
                        final PageSiteMap pageSiteMap = new PageSiteMap(pageID,
                                _currentLevel,
                                false, nodePage.getID(),
                                !childPageEnum.hasNext(),
                                contentPage.getTitles(ContentPage.LAST_UPDATED_TITLES),
                                _currentLevel, _defaultMaxLevel);
                        _jahiaPageSiteMap.add(pageSiteMap);
                    }
                }
            } else {
                final Map titles = new HashMap();
                final PageSiteMap pageSiteMap = new PageSiteMap(childObject.getObjectKey(), _currentLevel,
                        !nextChildPages.isEmpty(), nodePage.getID(),
                                                          !childPageEnum.hasNext(), titles,
                                                          _currentLevel, _defaultMaxLevel);
                _jahiaPageSiteMap.add(pageSiteMap);
                getAllPageChilds(childObject, nextChildPages.iterator(), context);
            }
        }
        _currentLevel--;
    }

    protected List getPageChilds(ContentObject object) {
        final ContentPage page = (ContentPage) object;
        final List pageChildsList = new ArrayList();        
        try {
            Iterator pageChildsEnum = page.getContentPageChilds(_user, _pageInfosFlag, null, true);
            if (pageChildsEnum == null) {
                pageChildsEnum = new ArrayList(0).iterator();
            }
            final ProcessingContext jParams = Jahia.getThreadParamBean();
            // We need to take care of the ranking and the sorters in order to deliver the pages in the correct order
            final TreeSet orderedPages = new TreeSet(new JahiaPageComparator(jParams, new HashMap()));
            while (pageChildsEnum.hasNext()) {
                orderedPages.add(pageChildsEnum.next());
            }

            if (_pageInfosFlag == (ContentPage.STAGING_PAGE_INFOS)) {
                return new ArrayList(orderedPages); // In this case shortcut the next steps for optmization.
            }

            // time based publishing state check
            final String operationMode;
            if (jParams != null) {
                operationMode = jParams.getOperationMode();
            } else {
                operationMode = ParamBean.EDIT;
            }

            // Precompute what should contain the site map regarding the page
            // infos flag and the language code.
            boolean paramBeanTest = ParamBean.NORMAL.equals(operationMode) || ParamBean.PREVIEW.equals(operationMode);
            boolean pageInfosFlagTest = (_pageInfosFlag == (ContentPage.ACTIVE_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS));
            final Iterator iterator = orderedPages.iterator();
            while (iterator.hasNext()) {
                final ContentPage contentPage = (ContentPage) iterator.next();
                if (_languageCode != null) {
                    if (!contentPage.hasEntries(_pageInfosFlag)) {
                        continue; // This page does not fill the criterions
                    }
                } else {
                    if (pageInfosFlagTest && !contentPage.hasEntries(_pageInfosFlag)) {
                        continue; // This page does not fill the criterions
                    }
                }
                if (paramBeanTest) {
                    JahiaObjectDelegate delegate = jahiaObjectManager.getJahiaObjectDelegate(contentPage.getObjectKey());
                    if (delegate != null && !delegate.isValid()) {
                        continue;
                    }
                }
                pageChildsList.add(contentPage);
            }
        } catch (JahiaException je) {
            logger.debug("Unable to find '" + page.getID() + "' child pages");
        }
        return pageChildsList;                
    }
    
    private EntryLoadRequest createEntryLoadRequest(int pageInfosFlag, String languageCode) {
        EntryLoadRequest loadRequest = null;
        List<Locale> langs = new ArrayList<Locale>();
        langs.add(EntryLoadRequest.SHARED_LANG_LOCALE);
        if (languageCode != null) {
            langs.add(
                org.jahia.utils.LanguageCodeConverters.languageCodeToLocale(
                languageCode));
        }
        if ( (pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if ( (pageInfosFlag & ContentPage.ACTIVE_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if ( (pageInfosFlag & ContentPage.ARCHIVED_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                     0, langs);
            loadRequest.setWithDeleted(true);
            loadRequest.setWithMarkedForDeletion(true);
        }        
        return loadRequest;
    }
    
    

    private int _currentLevel = 0;
    private int _maxLevel; // Contain the max page level in the tree
    private int _defaultMaxLevel;
    private JahiaUser _user;
    private int _pageInfosFlag;
    private String _languageCode;
    private boolean _directPageOnly = true;
    //  Added by PAP:
    private PagesFilter _pagesFilter;
    private EntryLoadRequest _loadRequest = null;        

    public JahiaUser getUser() {
        return _user;
    }

    // Available language code storage.
    private Map _availableLanguageCodeMap = new HashMap();

    protected List _jahiaPageSiteMap = new ArrayList(8);

    private static final transient Logger logger = Logger.getLogger(SiteMapViewHelper.class);

}
