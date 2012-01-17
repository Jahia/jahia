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

//                                   ____.
//                       __/\ ______|    |__/\.     _______
//            __   .____|    |       \   |    +----+       \
//    _______|  /--|    |    |    -   \  _    |    :    -   \_________
//   \\______: :---|    :    :           |    :    |         \________>
//           |__\---\_____________:______:    :____|____:_____\
//                                      /_____|
//
//                 . . . i n   j a h i a   w e   t r u s t . . .
//
/*
 * ----- BEGIN LICENSE BLOCK -----
 * Version: JCSL 1.0
 *
 * The contents of this file are subject to the Jahia Community Source License
 * 1.0 or later (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.jahia.org/license
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the rights, obligations and limitations governing use of the contents
 * of the file. The Original and Upgraded Code is the Jahia CMS and Portal
 * Server. The developer of the Original and Upgraded Code is JAHIA Ltd. JAHIA
 * Ltd. owns the copyrights in the portions it created. All Rights Reserved.
 *
 * The Shared Modifications are Jahia button taglibs.
 *
 * The Developer of the Shared Modifications is Jahia Solution S�rl.
 * Portions created by the Initial Developer are Copyright (C) 2002 by the
 * Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Sep 24 2002 Jahia Solutions S�rl: MAP Initial release.
 *
 * ----- END LICENSE BLOCK -----
 */

package org.jahia.taglibs.internal.uicomponents;

import org.jahia.utils.LanguageCodeConverters;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * <p>The Jahia Shared Modification is: Jahia Tag Libs</p>
 *
 * <p>Description:
 * Display a country flag image (if possible) corresponding to a given language (with optional image rollover support).
 *
 * Synopsis : <content:displayLanguageFlag
 *                     [code="<iso639>[_<iso3166>]]"
 *                      [href="<anchor href>"] [alt="<text>"]
 *                      [resourceOn="<relative path to an image>"]
 *                      [resourceOff="<relative path to an image>"]/>
 *
 * The language resource image is a composition of these both paramters :<br>
 * <iso639> : is the iso 639 language code. If this code is incorrect then the
 * code name is displayed as is.<br>
 * <iso3166> : Optional parameter that represent the iso 3166 country code.<br>
 *  <p>   examples : <br>
 *  &nbsp;&nbsp;&nbsp;              code="en_UK"<br>
 *  &nbsp;&nbsp;&nbsp;              code="en"<br>
 *  &nbsp;&nbsp;&nbsp;              code="fr_CH"<br>
 * href : This is the anchor tag < A> "href" parameter.<br>
 * alt : Correspond to the < IMG> tag "alt" parameter.<br>
 * resourceOn, resourceOff : Alternative resource defined by the relative
 * path from the JSP source file to an image file.</p>
 *
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 *
 * @jsp:tag name="displayLanguageFlag" body-content="empty"
 * description="Display a country flag image (if possible) corresponding to a given language.
 *
 * <p><i>Synopsis</i> : &lt;content:displayLanguageFlag [code=\"&lt;iso639&ht;[_&lt;iso3166&gt;]]\"  <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                     [href=\"&lt;anchor href&gt;\"] [alt=\"&lt;text&gt;\"] <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                     [resourceOn=\"&lt;relative path to an image&gt;\"] <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;                     [resourceOff=\"&lt;relative path to an image&gt;\"] /&gt; <br>
 * <p><attriInfo>
 * <br>
 * The language is a composition of these both paramters :<br>
 * <li>'iso639' : is the iso 639 language code. If this code is incorrect then the
 * code name is displayed as is.<br>
 * <li>'iso3166' : Optional parameter that represent the iso 3166 country code.<br>
 * <p>
 *   examples :
 *              &nbsp;&nbsp; code=\"en_UK\" ; will display : English<br>
 *              &nbsp;&nbsp; code=\"fr\" ; will display : fran�ais<br>
 *              &nbsp;&nbsp; code=\"fr_CH\" ; will display : fran�ais<br>
 *              &nbsp;&nbsp; code=\"ar_AS\" ; will display : \u0627\u0644\u0639\u0631\u0628\u064A\u0629 (arabic chars)<br>
 * </p>
 * <p><b>Example :</b>
 * <p>
 * &lt;content:displayLanguageFlag code=\"&lt;%=languageCode%&gt;\" <br>
   &nbsp;&nbsp;                             href=\"&lt;%=currentLanguageCode.equals(languageCode) ? \"\" : jData.gui().drawPageLanguageSwitch(languageCode)%&gt;\" <br>
   &nbsp;&nbsp;                             alt=\"&lt;%=languageName%>\"/&gt;
 * </attriInfo>"
 */
@SuppressWarnings("serial")
public class DisplayLanguageFlagTag extends AbstractJahiaTag {

    /**
     * @param code The language code coresponding to the iso 639 language code
     * list.
     *
     * @jsp:attribute name="src" required="false" rtexprvalue="true"
     * description="The language code coresponding to the iso 639 language code list (with or without the iso 3166 country code)
     * <p><attriInfo>More details on iso639 <a href='http://www.w3.org/WAI/ER/IG/ert/iso639.htm'>here</a>. More details on iso3166
     *  <a href='http://www.iso.org/iso/en/prods-services/iso3166ma/02iso-3166-code-lists/list-en1.html'>here</a>.
     * </attriInfo>"
     */
    public void setCode (String code) {
        _code = code;
    }

    /**
     * @param href The anchor href parameter.
     *
     * @jsp:attribute name="href" required="false" rtexprvalue="true"
     * description="the anchor href parameter.
     * <p><attriInfo>This is the anchor tag &lt;A&gt; \"href\" parameter.
     * </attriInfo>"
     */
    public void setHref (String href) {
        _href = href;
    }

    /**
     * @param resourceOn The path to a resource image file corresponding to the
     * active position when mouse is on it.
     *
     * @jsp:attribute name="resourceOn" required="false" rtexprvalue="true"
     * description="the path to a resource image file corresponding to the
     * active position when mouse is positioned over it.
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setResourceOn (String resourceOn) {
        _resourceOn = resourceOn;
    }

    /**
     * @param resourceOff The path to a resource image file corresponding to the
     * deactivate position when mouse doesn't point to it.
     *
     * @jsp:attribute name="resourceOff" required="false" rtexprvalue="true"
     * description="the path to a resource image file corresponding to the
     * deactivate position when mouse doesn't point to it.
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setResourceOff (String resourceOff) {
        _resourceOff = resourceOff;
    }

    /**
     * @param alt The alt parameter from the flag image.
     *
     * @jsp:attribute name="alt" required="false" rtexprvalue="true"
     * description="Defines a short description of the flag image.
     * <p><attriInfo>Correspond to the &lt;IMG&gt; tag \"alt\" parameter.
     * </attriInfo>"
     */
    public void setAlt (String alt) {
        _alt = alt;
    }

    public int doStartTag () {

        // Recover 'jData'
        // @todo FIXME : This code is repeated in a lot of button taglibs
        HttpServletRequest request = (HttpServletRequest) pageContext.
                                     getRequest();

        // Produce the HTML code
        try {
            JspWriter out = pageContext.getOut();
            StringBuffer str = new StringBuffer("");
            if (debug) {
                str.append("\n<!-- ======================================================================= -->\n");
                str.append("<!-- The following HTML code is generated by 'DisplayLanguageFlagTag' taglib -->\n");
                str.append("<!----------------------------------------------------------------------------->\n");
            }
            // Resolve file name
            String flagName = LanguageCodeConverters.languageCodeToLocale(_code).getDisplayCountry(Locale.ENGLISH).toLowerCase().replace(" ","_");
            
            String flagOnPath;
            if ("".equals(_resourceOn)) {
                flagOnPath = request.getContextPath() + "/css/images/flags/plain/flag_" + flagName + ".png";
            } else {
                flagOnPath = _resourceOn;
            }
            // Define a unique ID that identify the rollover
            String flagImgID = "flagImg" + String.valueOf(imgID++);
            // Contruct image HTML tag
            if (flagOnPath != null && !"".equals(flagName)) {
                String flagOffPath = flagOnPath;
                // Should an anchor be written.
                if (!"".equals(_href)) {
                    if ("".equals(_resourceOff)) {
                        flagOffPath = request.getContextPath() + "/css/images/flags/shadow/flag_" + flagName + ".png";
                    } else {
                        flagOffPath = _resourceOff;
                    }
                    if (flagOffPath == null) {
                        flagOffPath = "";
                    }
                    str.append("<a href=\"");
                    str.append(_href);
                    str.append("\"\n");
                    str.append("\tonMouseOut=\"MM_swapImgRestore()\"\n");
                    str.append("\tonMouseOver=\"MM_swapImage('");
                    str.append(flagImgID);
                    str.append("','','");
                    str.append(flagOnPath);
                    str.append("',1)\">\n");
                }
                // Write image HTML tag
                str.append("<img name=\"");
                str.append(flagImgID);
                str.append("\" border=\"0\" onload=\"fixPNG(this)\" alt=\"");
                str.append(_alt);
                str.append("\" src=\"");
                str.append("".equals(_href) ? flagOnPath : flagOffPath);
                str.append("\" />");
            } else { // No flag image resource found.
                if (!"".equals(_href)) {
                    str.append("<a href=\"");
                    str.append(_href);
                    str.append("\">");
                }
                str.append("<span style=\"margin:0.2em; background-color:#eaeaea; border: 1px solid #ccc; padding:0em 0.2em; text-transform:uppercase\">");
                str.append(_code);
                str.append("</span>");
                str.append(flagName);
            }
            if (!"".equals(_href)) {
                str.append("</a>");
            }
            if (debug) {
                str.append("\n<!-- ======================================================================= -->\n");
            }
            out.print(str.toString());
        } catch (IOException ioe) {
            logger.debug(ioe.toString());
        }
        return SKIP_BODY;
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        _code = "";
        _href = "";
        _resourceOn = "";
        _resourceOff = "";
        _alt = "";
        return EVAL_PAGE;
    }

    // Taglib parameters
    private String _code = "";
    private String _href = "";
    private String _resourceOn = "";
    private String _resourceOff = "";
    private String _alt = "";

    // Display debug information
    /** todo FIXME : change harcoded debug information */
    private boolean debug = false;
    private static long imgID = 0;

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(DisplayLanguageFlagTag.class);

}
