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

package org.jahia.services.htmlvalidator;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.bin.JahiaMultiActionController;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ProcessingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

/**
 * Performs WAI checking of the supplied HTML text.
 * 
 * @author Sergiy Shyrkov
 */
public class WCAGController extends JahiaMultiActionController {

    private static final XStream JSON_SERIALIZER = new XStream(new JsonHierarchicalStreamDriver() {
        public HierarchicalStreamWriter createWriter(Writer writer) {
            return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
        }
    });

    private static Logger logger = LoggerFactory.getLogger(WCAGController.class);

    private String toJSON(ValidatorResults validateResults) {
        return JSON_SERIALIZER.toXML(validateResults);
    }

    public void validate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // TODO add a role?
            // checkUserAuthorized();
            checkUserLoggedIn();

            String text = getParameter(request, "text");

            Locale locale = (Locale) request.getSession(true).getAttribute(
                    ProcessingContext.SESSION_UI_LOCALE);
            locale = locale != null ? locale : request.getLocale();

            if (logger.isDebugEnabled()) {
                logger.debug("Request received for validating text using locale '{}'. Text: {}",
                        locale, text);
            }

            ValidatorResults validateResults = new WAIValidator(locale).validate(text);

            response.setContentType("application/json; charset=UTF-8");
            String serialized = toJSON(validateResults);
            if (logger.isDebugEnabled()) {
                logger.debug("Validation results: {}", serialized);
            }
            response.getWriter().append(serialized);

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JahiaUnauthorizedException ue) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ue.getMessage());
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }
    }

}
