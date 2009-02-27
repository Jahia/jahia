/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

    function EnginePopup(contextPath, engineName )
    {
        var oldurl  = location.href;
        var stringA = "engineName/";
        var posA = oldurl.indexOf( stringA );
        if (posA != -1) {
            posA = posA + stringA.length;
            var posB = oldurl.indexOf( "/", posA );
            if (posB != -1) {
                oldurl = oldurl.substring( 0, posA ) + engineName + oldurl.substring( posB, oldurl.length );
            } else {
                posB = oldurl.indexOf( "?", posA );
                if (posB != -1) {
                    oldurl = oldurl.substring( 0, posA ) + engineName;
                } else {
                    oldurl = oldurl.substring( 0, posA ) + engineName;
                }
            }
        } else {
            var posB = oldurl.indexOf( "?" );
            //added by PAP to check if there is a request path segment parameter
            var posC = oldurl.indexOf( ";" );
            if (posB == -1 || (posC < posB && posC != -1)) {
                posB = posC;
            }
            if (posB != -1) {
                oldurl = oldurl.substring( 0, posB ) +"/engineName/" + engineName + oldurl.substring( posB, oldurl.length );
            } else {
            	if (oldurl.indexOf( contextPath ) != -1) {
                oldurl = oldurl + "/engineName/" + engineName;
            	} else {
            		oldurl = oldurl + contextPath + "/engineName/" + engineName;
            	}
            }
        }

        if (oldurl != location.href)
        {
            var params = "width=" + 450 + ",height=" + 500 + ",resizable=1,scrollbars=0,status=0";
            var myWin = window.open( oldurl, engineName , params );
        }
    }