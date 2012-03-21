/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.functions;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.Url;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RangeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspTagException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Custom functions, which are exposed into the template scope.
 *
 * @author Sergiy Shyrkov
 */
public class Functions {
    
    private static final Comparator<Map<String, Object>> DISPLAY_NAME_COMPARATOR = new Comparator<Map<String, Object>>() {
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            return StringUtils
                    .defaultString((String) o1.get("displayName"))
                    .toLowerCase()
                    .compareTo(
                            StringUtils.defaultString((String) o2.get("displayName")).toLowerCase());
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(Functions.class);

    public static String attributes(Map<String, Object> attributes) {
        StringBuilder out = new StringBuilder();

        for (Map.Entry<String, Object> attr : attributes.entrySet()) {
            out.append(attr.getKey()).append("=\"")
                    .append(attr.getValue() != null ? attr.getValue().toString() : "")
                    .append("\" ");
        }

        return out.toString();
    }

    /**
     * Checks if the provided target object can be found in the source. The
     * search is done, depending on the source parameter type. It can be either
     * {@link String}, {@link Collection} or an array of objects.
     *
     * @param source the source to search in
     * @param target the object to search for
     * @return <code>true</code> if the target object is present in the source
     */
    public static boolean contains(Object source, Object target) {
        if (source == null) {
            throw new IllegalArgumentException("The source cannot be null");
        }
        boolean found = false;
        if (source instanceof Collection<?>) {
            found = ((Collection<?>) source).contains(target);
        } else if (source instanceof Object[]) {
            found = ArrayUtils.contains((Object[]) source, target);
        } else {
            found = target != null ? source.toString().contains(target.toString()) : false;
        }

        return found;
    }

    public static int countOccurences(String initString, String searchString) {
        final String[] fullString = ("||||" + initString + "||||").split(searchString);
        return fullString.length - 1;
    }

    /**
     * Decode facet filter URL parameter
     * @param inputString enocded facet filter URL query parameter
     * @return decoded facet filter parameter
     */
    public static String decodeUrlParam(String inputString) {
        return Url.decodeUrlParam(inputString);
    }

    public static Object defaultValue(Object value, Object defaultValue) {
        return (value != null && (!(value instanceof String) || (((String) value)
                .length() > 0))) ? value : defaultValue;
    }


    public static java.lang.String displayLocaleNameWith(Locale localeToDisplay, Locale localeUsedForRendering) {
        return localeToDisplay.getDisplayName(localeUsedForRendering);
    }

    /**
     * Encode facet filter URL parameter
     * @param inputString facet filter parameter
     * @return filter encoded for URL query parameter usage
     */
    public static String encodeUrlParam(String inputString) {
        return Url.encodeUrlParam(inputString);
    }

    public static VanityUrl getDefaultVanityUrl(JCRNodeWrapper node) {
        try {
            VanityUrlService vanityUrlService = (VanityUrlService) SpringContextSingleton.getBean(VanityUrlService.class.getName());
            List<VanityUrl> l = vanityUrlService.getVanityUrls(node, node.getSession().getLocale().toString(), node.getSession());
            VanityUrl vanityUrl = null;
            for (VanityUrl v : l) {
                if (v.isDefaultMapping()) {
                    vanityUrl =  v;
                }
            }
            return vanityUrl;
        } catch (RepositoryException e) {

        }
        return null;
    }

    public static List<Map<String, Object>> getRolesForNode(JCRNodeWrapper node, boolean includeInherited, String roles, int limit, String sortType) {
        List<Map<String, Object>> results = new LinkedList<Map<String, Object>>();
        Map<String,List<String[]>> entries = node.getAclEntries();

        if (sortType != null && sortType.equalsIgnoreCase("latestFirst")) {
            entries = reverse(entries);
        }

        boolean sortByDisplayName = sortType != null && sortType.equalsIgnoreCase("displayName");
        int siteId = -1;
        
        JahiaUserManagerService userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaGroupManagerService groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        
        for (Map.Entry<String, List<String[]>> entry : entries.entrySet()) {
            Map<String, Object> m = new HashMap<String, Object>();
            String entryKey = entry.getKey();
            if (entryKey.startsWith("u:")) {
                JahiaUser u = userService.lookupUser(StringUtils.substringAfter(entryKey, "u:"));
                if (u == null) {
                    logger.warn("User {} cannot be found. Skipping.", StringUtils.substringAfter(entryKey, "u:"));
                    continue;
                }
                m.put("principalType","user");
                m.put("principal",u);
                if (sortByDisplayName) {
                    m.put("displayName", PrincipalViewHelper.getFullName(u));
                }
            } else if (entryKey.startsWith("g:")) {
                if (siteId == -1) {
                    try {
                        JCRSiteNode resolveSite = node.getResolveSite();
                        siteId = resolveSite != null ? resolveSite.getID() : 0;
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                JahiaGroup g = groupService.lookupGroup(siteId, StringUtils.substringAfter(entryKey, "g:"));
                if (g == null) {
                    logger.warn("Group {} cannot be found for site with ID={}. Skipping.", StringUtils.substringAfter(entryKey, "g:"), siteId);
                    continue;
                }
                m.put("principalType","group");
                m.put("principal",g);
                if (sortByDisplayName) {
                    m.put("displayName", PrincipalViewHelper.getFullName(g));
                }
            }

            for (String[] details : entry.getValue()) {
                if (details[1].equals("GRANT")) {
                    if (roles != null) {
                        if (!roles.contains(details[2])) {
                            continue;
                        }
                    }
                
                    if (!includeInherited) {
                        if (!details[0].equals(node.getPath())) {
                            continue;
                        }
                    }
                    if (!m.containsKey("roles")) {
                        m.put("roles", new LinkedList<String>());
                        results.add(m);
                    }
                    ((List)m.get("roles")).add(details[2]);
                }
            }
            
            if (limit > 0 && results.size() >= limit) {
                break;
            }
        }
        
        if (sortByDisplayName) {
            Collections.sort(results, DISPLAY_NAME_COMPARATOR);
        }
        
        return results;
    }

    public static Boolean hasScriptView(JCRNodeWrapper node, String viewName, RenderContext renderContext) {
        try {
            return RenderService.getInstance().resolveScript(new org.jahia.services.render.Resource(node, renderContext.getMainResource().getTemplateType(), viewName, renderContext.getMainResource().getContextConfiguration()), renderContext) != null;
        } catch (TemplateNotFoundException e) {
            //Do nothing
        } catch (RepositoryException e) {
           //Do nothing
        }
        return false;
    }

    /**
     * Checks if the current object is iterable so that it can be used in an c:forEach
     * tag.
     *
     * @param object the object to be checked if it is iterable
     * @return if the current object is iterable return true otherwise false
     */
    public static Boolean isIterable(Object o) {
        boolean isIt = false;
        if (o instanceof Object[] || o instanceof boolean[] || o instanceof byte[]
                || o instanceof char[] || o instanceof short[] || o instanceof int[]
                || o instanceof long[] || o instanceof float[] || o instanceof double[]
                || o instanceof Collection<?> || o instanceof Iterator<?>
                || o instanceof Enumeration<?> || o instanceof Map<?, ?> || o instanceof String) {
            isIt = true;
        }

        return isIt;
    }

    public static long length(Object obj) throws JspTagException {
        return (obj != null && obj instanceof RangeIterator) ? JCRContentUtils.size((RangeIterator) obj)
                : org.apache.taglibs.standard.functions.Functions.length(obj);
    }


    /**
     * Looks up the user by the specified user key (with provider prefix) or username.
     *
     * @param user the key or the name of the user to perform lookup for
     * @return the user for the specified user key or name or <code>null</code> if the corresponding user cannot be found
     * @throws IllegalArgumentException in case the specified user key is <code>null</code>
     */
    public static JahiaUser lookupUser(String user) throws IllegalArgumentException {
        if (user == null) {
            throw new IllegalArgumentException("Specified user key is null");
        }
        return user.startsWith("{") ? ServicesRegistry.getInstance().getJahiaUserManagerService()
                .lookupUserByKey(user) : ServicesRegistry.getInstance()
                .getJahiaUserManagerService().lookupUser(user);
    }

    public static boolean matches(String pattern, String str) {
        return Pattern.compile(pattern).matcher(str).matches();
    }

    public static Boolean memberOf(String groups) {
        boolean result = false;
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        final String[] groupArray = StringUtils.split(groups, ',');
        for (String aGroupArray : groupArray) {
            final String groupName = aGroupArray.trim();
            if (JCRSessionFactory.getInstance().getCurrentUser().isMemberOfGroup(jParams.getSiteID(), groupName)) {
                return true;
            }
        }

        return result;
    }

    public static Boolean notMemberOf(String groups) {
        boolean result = true;
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        final String[] groupArray = StringUtils.split(groups, ',');
        for (String aGroupArray : groupArray) {
            String groupName = aGroupArray.trim();
            if (JCRSessionFactory.getInstance().getCurrentUser().isMemberOfGroup(jParams.getSiteID(),
                    groupName)) {
                return false;
            }
        }

        return result;
    }

    public static String removeCacheTags(String txt) {
        return AggregateCacheFilter.removeEsiTags(txt);
    }

    public static String removeDuplicates(String initString, String separator) {
        final String[] fullString = initString.split(separator);
        StringBuilder finalString = new StringBuilder();
        String tmpString = initString;
        for (String s : fullString) {
            if (tmpString.contains(s)) {
                finalString.append(s);
                if (finalString.length() > 0) {
                    finalString.append(separator);
                }
                tmpString = tmpString.replaceAll(s, "");
            }
        }
        return finalString.toString();
    }

    public static String removeHtmlTags(String value) {
        Source source = new Source(value);
        TextExtractor textExtractor = source.getTextExtractor();
        textExtractor.setExcludeNonHTMLElements(true);
        textExtractor.setConvertNonBreakingSpaces(false);
        textExtractor.setIncludeAttributes(false);
        return textExtractor.toString();
    }
    
    /**
     * Reverse the content of a list. Only works with some List.
     *
     * @param list List<T> list to be reversed.
     * @return <code>java.util.List</code> the reversed list.
     */
    public static <T> List<T> reverse(Collection<T> list) {
        List<T> copy = new ArrayList<T>();
        copy.addAll(list);
        Collections.reverse(copy);
        return copy;
    }
    
    public static <T> Iterator<T> reverse(Iterator<T> it) {
        List<T> copy = new ArrayList<T>();
        while (it.hasNext()) {
            copy.add(it.next());
        }
        Collections.reverse(copy);
        return copy.iterator();
    }

    public static <T> Map<String, T> reverse(Map<String, T> orderedMap) {
        if (orderedMap == null || orderedMap.isEmpty()) {
            return orderedMap;
        }
        LinkedHashMap<String, T> reversed = new LinkedHashMap<String, T>(orderedMap.size());
        ListIterator<String> li = new LinkedList<String>(orderedMap.keySet())
                .listIterator(orderedMap.size());
        while (li.hasPrevious()) {
            String key = li.previous();
            reversed.put(key, orderedMap.get(key));
        }
        return reversed;
    }

    public static String stringConcatenation(String value, String appendix1, String appendix2) {
        final StringBuffer buff = new StringBuffer();
        if (value != null) {
            buff.append(value);
        }
        if (appendix1 != null) {
            buff.append(appendix1);
        }
        if (appendix2 != null) {
            buff.append(appendix2);
        }
        return buff.toString();
    }
}