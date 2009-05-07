/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.views.engines;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

/**
 *
 * <p>Title: Abstract Engine Action Class</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Jahia</p>
 * @author Khue Nguyen
 * @version 1.0
 */
public abstract class AbstractEngineAction extends Action {

    /**
    * Process the specified HTTP request, and create the corresponding HTTP
    * response (or forward to another web component that will create it).
    * Return an <code>ActionForward</code> instance describing where and how
    * control should be forwarded, or <code>null</code> if the response has
    * already been completed.
    *
    * @param mapping The ActionMapping used to select this instance
    * @param form The optional ActionForm bean for this request (if any)
    * @param request The HTTP request we are processing
    * @param response The HTTP response we are creating
    *
    * @exception IOException if an input/output error occurs
    * @exception ServletException if a servlet exception occurs
    */
    public abstract ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                                 throws IOException, ServletException;


    /**
     * Is the current session valid. Does it contain initialized session objects
     * or not ?
     *
     * @param request
     * @return
     */
    public abstract boolean isSessionValid(HttpServletRequest request);

    /**
     * Default errors Action Forward. If no errors, return a null ActionForward.
     *
     * @param mapping
     * @param request
     * @param errors
     * @return
     */
    public ActionForward errorsForward(ActionMapping mapping,
                                       HttpServletRequest request,
                                       ActionMessages errors){
        if(errors != null && !errors.isEmpty()){
            saveErrors(request,errors);
            return (new ActionForward(mapping.getInput()));
        }
        return null;
    }

    /**
     * Default implementation for logging an exception.
     *
     * Do a call to servlet.log(msg,ex)
     *
     * @param msg
     * @param ex
     */
    public void loggingException(String msg, Exception ex){
        servlet.log(msg,ex);
    }

    /**
     * Default implementation for logging a message.
     *
     * Do a call to servlet.log(msg)
     *
     * @param msg message
     */
    public void loggingMsg(String msg){
        servlet.log(msg);
    }

}
