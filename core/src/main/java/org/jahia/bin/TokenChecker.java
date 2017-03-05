/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin;

import org.apache.commons.collections.CollectionUtils;
import org.jahia.settings.SettingsBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenChecker {

    public static final int NO_TOKEN = 0;
    public static final int VALID_TOKEN = 1;
    public static final int INVALID_TOKEN = 2;
    public static final int INVALID_HIDDEN_FIELDS = 3;
    public static final int INVALID_CAPTCHA = 4;

    public static int checkToken(HttpServletRequest req, HttpServletResponse resp, Map<String, List<String>> parameters) throws UnsupportedEncodingException {
        String token = parameters.get("form-token")!=null?parameters.get("form-token").get(0):null;
        if (token != null) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, List<String>>> toks = (Map<String, Map<String, List<String>>>) req.getSession().getAttribute("form-tokens");
            if (toks != null && toks.containsKey(token)) {
                Map<String, List<String>> m = toks.get(token);
                if (m == null) {
                    return INVALID_TOKEN;
                }
                Map<String, List<String>> values = new HashMap<String, List<String>>(m);
                if (!values.remove(Render.ALLOWS_MULTIPLE_SUBMITS).contains("true")) {
                    toks.remove(token);
                }
                values.remove(Render.DISABLE_XSS_FILTERING);

                // Validate form token
                List<String> stringList1 = values.remove("form-action");
                String formAction = stringList1.isEmpty()?null:stringList1.get(0);
                String characterEncoding = SettingsBean.getInstance().getCharacterEncoding();
                String requestURI = req.getRequestURI();
                if (req.getQueryString() != null) {
                    requestURI += "?" + req.getQueryString();
                }
                if (formAction == null ||
                        (!URLDecoder.decode(requestURI, characterEncoding).equals(URLDecoder.decode(formAction, characterEncoding)) &&
                        !URLDecoder.decode(resp.encodeURL(requestURI), characterEncoding).equals(URLDecoder.decode(formAction, characterEncoding)))
                        ) {
                    return INVALID_HIDDEN_FIELDS;
                }
                if (!req.getMethod().equalsIgnoreCase(values.remove("form-method").get(0))) {
                    return INVALID_HIDDEN_FIELDS;
                }
                for (Map.Entry<String, List<String>> entry : values.entrySet()) {
                    List<String> stringList = entry.getValue();
                    List<String> parameterValues = parameters.get(entry.getKey());
                    if (parameterValues == null || !CollectionUtils.isEqualCollection(stringList, parameterValues)) {
                        if (entry.getKey().equals(Render.CAPTCHA)) {
                            return INVALID_CAPTCHA;
                        }
                        return INVALID_HIDDEN_FIELDS;
                    }
                }
                return VALID_TOKEN;
            }
            return INVALID_TOKEN;
        }
        return NO_TOKEN;
    }
}
