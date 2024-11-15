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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
// $Id$
//


package org.jahia.exceptions;

import javax.servlet.http.HttpServletResponse;


/**
 * This exception is raised when the user tries to access a resource to which
 * he has no permissions.
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class JahiaForbiddenAccessException extends JahiaException
{

    private static final long serialVersionUID = -1353013248036003416L;

    //-------------------------------------------------------------------------
    /** Default constructor
     *
     * @param   pageID  The not-found requested page ID.
     */
    public JahiaForbiddenAccessException () {
        this(null);
    }

    public JahiaForbiddenAccessException(String jahiaErrorMsg) {
        super("403 Access forbidden", jahiaErrorMsg != null ? jahiaErrorMsg
                : "403 Access forbidden", SECURITY_ERROR, ERROR_SEVERITY);
    }

    @Override
    public int getResponseErrorCode() {
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
