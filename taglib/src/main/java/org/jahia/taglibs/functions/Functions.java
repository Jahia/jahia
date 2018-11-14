/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.taglibs.functions;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.jahia.utils.CollectionUtils;
import org.jahia.utils.Patterns;
import org.jahia.utils.Url;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RangeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
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

    private static Map<String, Pattern> PATTERN_CACHE = Collections.synchronizedMap(CollectionUtils.lruCache(1000000));

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
        return WordUtils.capitalizeFully(localeToDisplay.getDisplayName(localeUsedForRendering));
    }

    public static String getLangIcon(Locale localeToDisplay) {
        if("".equals(localeToDisplay.getCountry()))
            return "flag_" + localeToDisplay.getLanguage().toLowerCase() + "_on";
        else
            return "flag_" + Patterns.SPACE.matcher(localeToDisplay.getDisplayCountry(Locale.ENGLISH).toLowerCase()).replaceAll("_");
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

    public static List<Map<String, Object>> getRolesForNode(JCRNodeWrapper node, boolean includeInherited, boolean expandGroups, String roles, int limit, String sortType) {
        List<Map<String, Object>> results;

        boolean sortByDisplayName = sortType != null && sortType.equalsIgnoreCase("displayName");
        results = JCRContentUtils.getRolesForNode(node, includeInherited, expandGroups, roles, limit,sortType != null && sortType.equalsIgnoreCase("latestFirst"));
        if (sortByDisplayName) {
            for (Map<String, Object> result : results) {
                result.put("displayName", PrincipalViewHelper.getFullName((JCRNodeWrapper) result.get("principal")));
            }
            Collections.sort(results, DISPLAY_NAME_COMPARATOR);
        }
        return results;
    }

    /**
     * Checks if the provided node has the specified view.
     * 
     * @param node
     *            the node to check the view for
     * @param viewName
     *            the view name
     * @param renderContext
     *            current rendering context
     * @return <code>true</code> if the provided node has the specified view; <code>false</code> otherwise
     */
    public static Boolean hasScriptView(JCRNodeWrapper node, String viewName, RenderContext renderContext) {
        return RenderService.getInstance().hasView(node, viewName, renderContext.getMainResource().getTemplateType(),
                renderContext);
    }

    /**
     * Checks if the current object is iterable so that it can be used in an c:forEach
     * tag.
     *
     * @param o the object to be checked if it is iterable
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

    /**
     * Joins the elements of the provided array/collection/iterator into a single String containing the provided elements with specified
     * separator.
     * 
     * @param elements
     *            the set of values to join together, may be null
     * @param separator
     *            the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null elements input
     */
    public static String join(Object elements, String separator) {
        if (elements == null) {
            return null;
        }

        if (elements instanceof Object[]) {
            return StringUtils.join((Object[]) elements, separator);
        } else if (elements instanceof Collection<?>) {
            return StringUtils.join((Collection<?>) elements, separator);
        } else if (elements instanceof Iterator<?>) {
            return StringUtils.join((Iterator<?>) elements, separator);
        } else if (elements instanceof Enumeration<?>) {
            return StringUtils.join(EnumerationUtils.toList((Enumeration<?>) elements), separator);
        } else if (elements instanceof Map<?, ?>) {
            return StringUtils.join(((Map<?, ?>) elements).keySet(), separator);
        } else if (elements instanceof String) {
            return (String) elements;
        }

        throw new IllegalArgumentException("Cannot handle the elements of type " + elements.getClass().getName());
    }

    public static long length(Object obj) throws JspTagException {
        return (obj != null && obj instanceof RangeIterator) ? JCRContentUtils.size((RangeIterator) obj)
                : org.apache.taglibs.standard.functions.Functions.length(obj);
    }



    public static boolean matches(String pattern, String str) {
        return getCompiledPattern(pattern).matcher(str).matches();
    }

    private static Pattern getCompiledPattern(String pattern) {
        Pattern compiledPattern = PATTERN_CACHE.get(pattern);
        if (compiledPattern == null) {
            compiledPattern = Pattern.compile(pattern);
            PATTERN_CACHE.put(pattern, compiledPattern);
        }
        return compiledPattern;
    }

    public static String removeCacheTags(String txt) {
        return AggregateCacheFilter.removeCacheTags(txt);
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
        if (value == null || value.length() == 0) {
            return value;
        }
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
        final StringBuilder buff = new StringBuilder();
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
    
    public static String sqlEncode(String s) {
        return JCRContentUtils.sqlEncode(s);
    }
    
    public static String xpathPathEncode(String s) {
        return JCRContentUtils.stringToJCRPathExp(s);
    }    
    
    public static String modulePath(HttpServletRequest req, String moduleId) {
        return req.getContextPath() + "/modules/" + moduleId;
    }
    
    /**
     * Returns the first parent of the specified node, which has the ACL inheritance broken. If not found, null<code>null</code> is
     * returned.
     * 
     * @param node
     *            the node to search parent for
     * 
     * @return the first parent of the specified node, which has the ACL inheritance broken. If not found, null<code>null</code> is returned
     */
    public static JCRNodeWrapper getParentWithAclInheritanceBroken(JCRNodeWrapper node) {
        try {
            return JCRContentUtils.getParentWithAclInheritanceBroken(node);
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            } else {
                logger.warn("Unable to get parent of a node " + node.getPath()
                        + " with ACL inheritance break. Cause: " + e.getMessage());
            }
        }

        return null;
    }

    public static String formatISODate(java.lang.String dateToParse, String pattern, Locale locale){
        try{
            DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(dateToParse);
            return DateTimeFormat.forPattern(pattern).withLocale(locale != null ? locale : Locale.ENGLISH).print(!dateToParse.endsWith("Z") ? dateTime : dateTime.toDateTime(DateTimeZone.forID("UTC")));
        } catch (Exception e) {
            logger.debug("Unable to parse date:"+dateToParse,e);
            return null;
        }
    }
    
    /**
     * Detects the current site key using data, available in the supplied request object.
     * 
     * @param request
     *            the current HTTP request object
     * @return the current site key
     * @since 7.1.2.6
     */
    public static String getCurrentSiteKey(HttpServletRequest request) {
        RenderContext ctx = (RenderContext) request.getAttribute("renderContext");
        if (ctx != null && ctx.getSite() != null) {
            return ctx.getSite().getSiteKey();
        }

        URLResolver urlResolver = (URLResolver) request.getAttribute("urlResolver");
        if (urlResolver != null) {
            return urlResolver.getSiteKey();
        }

        try {
            return ((URLResolverFactory) SpringContextSingleton.getBean("urlResolverFactory"))
                    .createURLResolver(request.getPathInfo(), request.getServerName(), request).getSiteKey();
        } catch (Exception e) {
            logger.warn("Unable to detect current site key based on the data from current HTTP request. Cause: "
                    + e.getMessage(), e);

            return null;
        }
    }
}