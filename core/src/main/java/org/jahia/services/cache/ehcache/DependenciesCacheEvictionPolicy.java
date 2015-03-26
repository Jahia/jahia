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
package org.jahia.services.cache.ehcache;

import net.sf.ehcache.Element;
import net.sf.ehcache.store.LruPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 * Created : 24/03/15
 */
public class DependenciesCacheEvictionPolicy extends LruPolicy {
    private static Logger logger = LoggerFactory.getLogger(DependenciesCacheEvictionPolicy.class);

    /**
     * The name of this policy as a string literal
     */
    public static final String NAME = "DependenciesAllBeforeLRU";

    /**
     * @return the name of the Policy. Inbuilt examples are LRU, LFU and FIFO.
     */
    public String getName() {
        return NAME;
    }

    @Override
    public boolean compare(Element element1, Element element2) {
        if(logger.isDebugEnabled()){
            logger.debug("Comparing desirableness of element:\n"+element1+"\nto element:\n"+element2);
        }
        return ((Set<String>) element1.getObjectValue()).contains("ALL") || !((Set<String>) element2.getObjectValue()).contains("ALL") && super.compare(element1, element2);
    }
}
