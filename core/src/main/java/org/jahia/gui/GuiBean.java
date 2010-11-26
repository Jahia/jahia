/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//  GuiBean
//  EV      03.11.2000
//	DJ		02.02.2001 - added ACL check for link display
//  JB      16.05.2001
//

package org.jahia.gui;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Modified and cleaned by Xavier Lawrence
 */
public class GuiBean {

    private final ProcessingContext processingContext;

    /**
     * constructor
     * EV    03.11.2000
     */
    public GuiBean(final ProcessingContext jContext) {
        this.processingContext = jContext;
    } // end constructor

    public ProcessingContext params() {
        return processingContext;
    }


    /**
     * tests if Jahia is in edition mode
     * JB   25.04.2001
     */
    public boolean isEditMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.EDIT);
        } else {
            return false;
        }
    } // end isEditMode

    public boolean isNormalMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.NORMAL);
        } else {
            return false;
        }
    }

    public boolean isCompareMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.COMPARE);
        } else {
            return false;
        }
    }

    public boolean isPreviewMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.PREVIEW);
        } else {
            return false;
        }
    }

    /**
     * tests if the current user is logged
     * JB   25.04.2001
     */
    public boolean isLogged() {
        if (processingContext != null) {
            final String theUserName = getUser().getUsername();
            if (!theUserName.equals("guest")) {
                return true;
            }
        }
        return false;
    } // end isLogged


    /**
     * tests if the browser is Netscape
     * JB   16.05.2001
     */
    public boolean isNS() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla") != -1) {
                if (userAgent.indexOf("MSIE") == -1) {
                    return true;
                }
            }
        }
        return false;
    } // end isNS


    /**
     * tests if the browser is Netscape 4.x.
     * JB   16.05.2001
     */
    public boolean isNS4() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla/4") != -1) {
                if (userAgent.indexOf("MSIE") == -1) {
                    return true;
                }
            }
        }
        return false;
    } // end isNS4


    /**
     * tests if the browser is Netscape 6.x.
     * JB   16.05.2001
     */
    public boolean isNS6() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla/5") != -1) {
                return true;
            }
        }
        return false;
    } // end isNS6

    /**
     * tests if the browser is Internet Explorer
     * JB   16.05.2001
     */
    public boolean isIE() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE

    /**
     * tests if the browser is Internet Explorer
     * JB   16.05.2001
     */
    public static boolean isIe(final HttpServletRequest req) {
        final String userAgent;
        final Enumeration<?> userAgentValues = req.getHeaders("user-agent");
        if (userAgentValues.hasMoreElements()) {
            // we only take the first value.
            userAgent = (String) userAgentValues.nextElement();
        } else {
            userAgent = null;
        }

        if (userAgent != null) {
            if (userAgent.indexOf("MSIE") != -1) {
                return true;
            }
        }
        return false;
    } // end isIe


    /**
     * tests if the browser is Internet Explorer 4.x.
     * JB   16.05.2001
     */
    public boolean isIE4() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 4") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE4


    /**
     * tests if the browser is Internet Explorer 5.x.
     * JB   16.05.2001
     */
    public boolean isIE5() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 5") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE5


    /**
     * isIE6
     * JB   16.05.2001
     */
    public boolean isIE6() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 6") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE6

    public boolean isIE7() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 7") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE7

    /**
     * isOpera
     */
    public boolean isOpera() {
        String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.indexOf("opera") != -1) {
                return true;
            }
        }
        return false;
    } // end isOpera

    /**
     * isWindow
     * JB   13.11.2001
     */
    public boolean isWindow() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Win") != -1) {
                return true;
            }
        }
        return false;
    } // end isWindow


    /**
     * isUnix
     * JB   13.11.2001
     */
    public boolean isUnix() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("X11") != -1) {
                return true;
            }
        }
        return false;
    } // end isUnix


    /**
     * isMac
     * JB   13.11.2001
     */
    public boolean isMac() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mac") != -1) {
                return true;
            }
        }
        return false;
    } // end isMac


    private JahiaUser getUser() {
        return processingContext != null ? processingContext.getUser() : null;
    }

}