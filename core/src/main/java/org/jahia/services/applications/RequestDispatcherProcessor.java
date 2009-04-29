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
 package org.jahia.services.applications;

import javax.servlet.ServletException;

/**
 * For pre and post request forward and include processing
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public interface RequestDispatcherProcessor {
    public abstract void preForward (ServletIncludeRequestWrapper request,
                                     ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException;

    public abstract void postForward (ServletIncludeRequestWrapper request,
                                      ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException;

    public abstract void preInclude (ServletIncludeRequestWrapper request,
                                     ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException;

    public abstract void postInclude (ServletIncludeRequestWrapper request,
                                      ServletIncludeResponseWrapper response)
            throws ServletException, java.io.IOException;

}
