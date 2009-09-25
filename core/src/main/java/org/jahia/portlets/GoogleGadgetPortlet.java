/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.portlets;

import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;
import net.htmlparser.jericho.StartTag;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: loom
 * Date: Jan 22, 2009
 * Time: 3:03:26 PM
 */
public class GoogleGadgetPortlet extends JahiaPortlet {

    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        renderResponse.setContentType("text/html");
        
        EntryPointInstance epi  = (EntryPointInstance) renderRequest.getAttribute("EntryPointInstance");
        if (epi != null ) {
            try {
                JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(renderRequest.getRemoteUser());
                Node node = JCRSessionFactory.getInstance().getThreadSession(user).getNodeByUUID(epi.getID());
                String htmlCode = node.getProperty("code").getString();

                String scriptURL = null;
                String height = "";
                Source source = new Source(htmlCode);
                source = new Source((new SourceFormatter(source)).toString());
                List<StartTag> scriptTags = source.getAllStartTags(HTMLElementName.SCRIPT);
                for (StartTag curScriptTag : scriptTags) {
                    if ((curScriptTag.getAttributeValue("src") != null) &&
                       (!curScriptTag.getAttributeValue("src").equals("")) ) {
                        scriptURL = curScriptTag.getAttributeValue("src");
                        Pattern heightPattern = Pattern.compile(".*\\&h\\=(.*?)\\&.*");
                        Matcher heightMatcher = heightPattern.matcher(scriptURL);
                        if (heightMatcher.matches()) {
                            height = " height=\"" + heightMatcher.group(1) + "\"";
                        }
                    }
                }

                PrintWriter pw = renderResponse.getWriter();
                if (scriptURL != null) {
                    scriptURL = scriptURL.replace("output=js", "output=html");
                    pw.print("<iframe frameborder=\"0\" border=\"0\" scrolling=\"no\" marginHeight=\"0\" marginWidth=\"0\" allowTransparency=\"true\" width=\"100%\" "+height+" src=\"");
                    pw.print(scriptURL);
                    pw.print("\"></iframe>");
                    pw.print("<center><table><tr><td style=\"text-align: left;\"><div style=\"height: 20px;\"><a href=\"http://fusion.google.com/ig/add?synd=open&amp;source=ggyp&amp;moduleurl=");
                    int i = scriptURL.indexOf("url=") + 4;
                    pw.print(scriptURL.substring(i, scriptURL.indexOf("&",i)));
                    pw.print("\" target=\"_top\"><img src=\"http://www.gmodules.com/ig/images/plus_google.gif\" style=\"border: 0pt none ; height: 17px; width: 68px;\"/></a></div></td><td style=\"text-align: right; vertical-align: middle; height: 18px;\"><div><a href=\"http://www.google.com/webmasters/gadgets.html\" style=\"font-size: 10px; color: rgb(0, 0, 204); text-decoration: underline;\" target=\"_top\"> Gadgets</a><span style=\"font-size: 10px; color: rgb(0, 0, 204);\"> powered by Google</span></div></td></tr></table></center>");
                } else {
                    pw.print("Couldn't render Google Gadget because the URL couldn't be resolved.");
                }
            } catch (RepositoryException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

}
