/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.htmlvalidator;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.api.Constants;
import org.jahia.bin.JahiaMultiActionController;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.AbstractJsonWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

/**
 * Performs WAI checking of the supplied HTML text.
 *
 * @author Sergiy Shyrkov
 */
public class WCAGController extends JahiaMultiActionController {

    private static Logger log = LoggerFactory.getLogger(WCAGController.class);

    private static class Holder {
        static XStream serializer = createSerializer();

        private static XStream createSerializer() {

            XStream xstream = new XStream(new JsonHierarchicalStreamDriver() {
                @Override
                public HierarchicalStreamWriter createWriter(Writer writer) {
                    return new JsonWriter(writer, AbstractJsonWriter.DROP_ROOT_MODE);
                }
            });
            XStream.setupDefaultSecurity(xstream);
            return xstream;
        }
    }

    private static XStream getJSonSerializer() {
        return Holder.serializer;
    }

    private String toJSON(ValidatorResults validateResults) {
        return getJSonSerializer().toXML(validateResults);
    }

    public void validate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            checkUserLoggedIn();

            String text = getParameter(request, "text");

            Locale locale = (Locale) request.getSession(true).getAttribute(
                    Constants.SESSION_UI_LOCALE);
            locale = locale != null ? locale : request.getLocale();

            if (log.isDebugEnabled()) {
                log.debug("Request received for validating text using locale '{}'. Text: {}",
                        locale, text);
            }

            ValidatorResults validateResults = new WAIValidator(locale).validate(text);

            response.setContentType("application/json; charset=UTF-8");
            String serialized = toJSON(validateResults);
            if (log.isDebugEnabled()) {
                log.debug("Validation results: {}", serialized);
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
