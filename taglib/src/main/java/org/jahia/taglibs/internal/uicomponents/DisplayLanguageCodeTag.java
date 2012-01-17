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

import org.apache.commons.lang.StringEscapeUtils;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.LanguageCodeConverters;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * <p>Title: Jahia Tag Lib</p>
 * <p>Description:
 * Display a language in the country language format.<br>
 * Synopsis : < jahia:displayLanguageFlag code="'iso639'[_'iso3166']"<br>
 *            [href="'anchor href'"] [style="className"]<br>
 * <br>
 * The language is a composition of these both paramters :<br>
 * <li>'iso639' : is the iso 639 language code. If this code is incorrect then the
 * code name is displayed as is.<br>
 * <li>'iso3166' : Optional parameter that represent the iso 3166 country code.<br>
 * <p>  examples : <br>
 *  &nbsp;&nbsp;&nbsp;            code="en_UK" ; will display : English<br>
 *  &nbsp;&nbsp;&nbsp;            code="fr" ; will display : fran�ais<br>
 *  &nbsp;&nbsp;&nbsp;            code="fr_CH" ; will display : fran�ais<br>
 *  &nbsp;&nbsp;&nbsp;            code="ar_AS" ; will display : \u0627\u0644\u0639\u0631\u0628\u064A\u0629 (arabic chars)<br>
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 *
 * @jsp:tag name="displayLanguageCode" body-content="empty"
 * description="Display a language in the country language format corresponding to a language code
 * <p><attriInfo>
 * <br>
 * The language is a composition of these both paramters :<br>
 * <li>'iso639' : is the iso 639 language code. If this code is incorrect then the
 * code name is displayed as is.<br>
 * <li>'iso3166' : Optional parameter that represent the iso 3166 country code.<br>
 * <p>
 *   examples : <br>
 *              &nbsp; code=\"en_UK\" ; will display : English<br>
 *              &nbsp; code=\"fr\" ; will display : fran�ais<br>
 *              &nbsp; code=\"fr_CH\" ; will display : fran�ais<br>
 *              &nbsp; code=\"ar_AS\" ; will display : \u0627\u0644\u0639\u0631\u0628\u064A\u0629 (arabic chars)<br>
 * </p>
 * <p><b>Example :</b>
 * <p>
 * &lt;content:displayLanguageCode  code=\"&lt;%=jParams.getLocale().getLanguage()%&gt;\" href=\"&lt;%=jData.gui().drawPageLanguageSwitch(languageCode)%&gt;\"/&gt;
 * </attriInfo>"
 */
@SuppressWarnings("serial")
public class DisplayLanguageCodeTag extends AbstractJahiaTag {

    /**
     * @param code The language code coresponding to the iso 639 language code
     * list.
     *
     * @jsp:attribute name="code" required="true" rtexprvalue="true"
     * description="The language code coresponding to the iso 639 language code list (with or without the iso3166 country code)
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
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setHref (String href) {
        _href = href;
    }

    /**
     *
     * @param style The class CSS defined in the view interface.
     *
     * @jsp:attribute name="style" required="false" rtexprvalue="true"
     * description="the class CSS defined in the view interface.
     * <p><attriInfo>Defaulted to no CSS style at all.
     *  </attriInfo>"
     */
    public void setStyle (String style) {
        _style = style;
    }

    public int doStartTag () {

        // Produce the HTML code
        try {
            JspWriter out = pageContext.getOut();
            StringBuffer str = new StringBuffer("");
            if (debug) {
                str.append("\n<!-- ======================================================================= -->\n");
                str.append("<!-- The following HTML code is generated by 'DisplayLanguageCodeTag' taglib -->\n");
                str.append("<!----------------------------------------------------------------------------->\n");
            }
            if (!"".equals(_href)) {
                str.append("<a ");
                if (!"".equals(_style)) {
                    str.append("class=\"");
                    str.append(_style);
                    str.append("\" ");
                }
                str.append("href=\"");
                str.append(_href);
                str.append("\">");
            }
            Locale localeLangToDisplay = LanguageCodeConverters.
                                         languageCodeToLocale(_code);
            str.append(StringEscapeUtils.escapeHtml(localeLangToDisplay.
                                          getDisplayLanguage(
                localeLangToDisplay)));
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
        _style = "";
        return EVAL_PAGE;
    }

    // Taglib parameters
    private String _code = "";
    private String _href = "";
    private String _style = "";

    // Display debug information
    /** @todo FIXME : change harcoded debug information */
    private boolean debug = false;

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(DisplayLanguageCodeTag.class);

}
