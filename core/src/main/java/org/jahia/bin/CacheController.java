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

package org.jahia.bin;

import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Performs various notification-related tasks.
 * 
 * @author Sergiy Shyrkov
 */
public class CacheController extends JahiaMultiActionController {

    private static Logger logger = LoggerFactory.getLogger(CacheController.class);

    private CacheService cacheService;

    public void flushByName(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            checkUserAuthorized();

            String name = getParameter(request, "name");

            if (logger.isDebugEnabled()) {
                logger.debug("Request received for flushing cache '{}'", name);
            }
            Cache<?, ?> cache = cacheService.getCache(name);
            if (cache != null) {
                cache.flush(Boolean.valueOf(getParameter(request, "propagate", "true")));
                logger.info("Content of the cache '{}' successfully flushed.", name);
            } else {
                throw new JahiaBadRequestException("Unable to find cache for name '" + name + "'");
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JahiaUnauthorizedException ue) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
}
