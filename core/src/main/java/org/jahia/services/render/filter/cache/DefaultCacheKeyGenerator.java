/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.render.filter.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator {
    private static Logger logger = LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);
    private List<CacheKeyPartGenerator> partGenerators;
    private List<String> fields;

    public List<CacheKeyPartGenerator> getPartGenerators() {
        return partGenerators;
    }

    public void setPartGenerators(List<CacheKeyPartGenerator> partGenerators) {
        this.partGenerators = partGenerators;
        this.fields = new ArrayList<String>();
        for (CacheKeyPartGenerator generator : partGenerators) {
            if (fields.contains(generator.getKey())) {
                throw new RuntimeException("Cannot register key part generator with existing key " + generator.getKey() + " , " + generator);
            }
            fields.add(generator.getKey());
        }
    }

    public String generate(Resource resource, RenderContext renderContext, Properties properties) {
        return StringUtils.join(getArguments(resource, renderContext, properties), "@@");
    }

    private Object[] getArguments(Resource resource, RenderContext renderContext, Properties properties) {
        List<String> args = new LinkedList<String>();
        for (CacheKeyPartGenerator generator : partGenerators) {
            args.add(generator.getValue(resource, renderContext, properties));
        }
        return args.toArray(new String[args.size()]);
    }

    public Map<String, String> parse(String key) {
        String[] values = getSplit(key);
        Map<String, String> result = new LinkedHashMap<String, String>(fields.size());
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            result.put(fields.get(i), value == null || value.equals("null") ? null : value);
        }
        return result;
    }

    public String replaceField(String key, String fieldName, String newValue) {
        String[] args = getSplit(key);
        args[fields.indexOf(fieldName)] = newValue;
        return StringUtils.join(args, "@@");
    }

    public CacheKeyPartGenerator getPartGenerator(String field) {
        return partGenerators.get(fields.indexOf(field));
    }

    public String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        String[] args = getSplit(key);
        String[] newArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            newArgs[i] = partGenerators.get(i).replacePlaceholders(renderContext,value);
        }
        String s = StringUtils.join(newArgs,"@@");
//        if (SettingsBean.getInstance().isProductionMode()) {
//            try {
//                byte[] b = DigestUtils.getSha512Digest().digest(s.getBytes("UTF-8"));
//                StringWriter sw = new StringWriter();
//                Base64.encode(b, 0, b.length, sw);
//                return sw.toString();
//            } catch (Exception e) {
//                logger.warn("Issue while digesting key",e);
//            }
//        }
        return s;
    }

    private String[] getSplit(String key) {
        String[] res = new String[fields.size()];
        int index = 0;
        int start = 0;
        int end;
        while ((end = key.indexOf("@@",start)) > -1) {
            res[ index ++ ] = key.substring(start,end);
            start = end + 2;
        }
        res[ index ++ ] = key.substring(start);
        while (index < res.length) res[ index ++ ] = "";
        return res;
    }

}
