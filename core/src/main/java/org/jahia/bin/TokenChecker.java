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
