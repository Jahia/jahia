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
package org.jahia.portlets;

import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.registries.ServicesRegistry;

import javax.portlet.*;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.SourceFormatter;
import au.id.jericho.lib.html.StartTag;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Jan 22, 2009
 * Time: 3:03:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleGadgetPortlet extends GenericPortlet {

    private static final String DEFINITIONS = "definitions.cnd";

    private static Map<String, String> defs = new HashMap<String, String>();

    private String porletType;

    public GoogleGadgetPortlet() {
        super();
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);

        porletType = portletConfig.getInitParameter("portletType");

        defs.put(getPortletName(), porletType);
    }

    public static String getPortletDefinition(String portletName) {
        return defs.get(portletName);
    }

    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException, IOException {
        renderResponse.setContentType("text/html");
        
        EntryPointInstance epi  = (EntryPointInstance) renderRequest.getAttribute("EntryPointInstance");
        if (epi != null ) {
            try {
                JahiaUser user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(renderRequest.getRemoteUser());
                Node node = ServicesRegistry.getInstance().getJCRStoreService().getNodeByUUID(epi.getID(), user);
                String htmlCode = node.getProperty("code").getString();

                String scriptURL = null;
                String height = "";
                Source source = new Source(htmlCode);
                source = new Source((new SourceFormatter(source)).toString());
                List<StartTag> scriptTags = source.findAllStartTags("script");
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
