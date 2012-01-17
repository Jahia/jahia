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
