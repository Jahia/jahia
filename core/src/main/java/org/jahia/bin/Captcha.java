/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import com.google.code.kaptcha.impl.DefaultKaptcha;

import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.utils.WebUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controller for generating a captcha image.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 10 mars 2010
 */
public class Captcha extends JahiaController {
	
    private DefaultKaptcha captchaProducer;

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String token = getParameter(request, "token");
            @SuppressWarnings("unchecked")
            Map<String,Map<String,List<String>>> toks = (Map<String,Map<String,List<String>>>) request.getSession().getAttribute("form-tokens");
            if (toks == null || !toks.containsKey(token)) {
                throw new JahiaBadRequestException("Unknown form token.");
            }
            // return a jpeg
            response.setContentType("image/jpeg");
    
            WebUtils.setNoCacheHeaders(response);
    
            // create the text for the image
            String capText = captchaProducer.createText();
    
            // store the text in the session
            toks.get(token).put(Render.CAPTCHA, Arrays.asList(capText));
    
            // create the image with the text
            BufferedImage bi = captchaProducer.createImage(capText);
    
            ServletOutputStream out = response.getOutputStream();
    
            // write the data out
            ImageIO.write(bi, "jpg", out);
            try {
                out.flush();
            } finally {
                out.close();
            }
            
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }

        return null;
    }

    public void setCaptchaProducer(DefaultKaptcha captchaProducer) {
        this.captchaProducer = captchaProducer;
    }

    public static String getCaptchaServletPath() {
        return "/cms/captcha";
    }
}
