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
package org.jahia.ajax.gwt.linkchecker.server;

import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaCheckedLink;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 15:26:28
 */
public class LinkChecker {

    private final static Logger logger = Logger.getLogger(LinkChecker.class);

    private List<GWTJahiaCheckedLink> m_links;
    private boolean running = true;

    private LinkChecker() {
        m_links = Collections.synchronizedList(new ArrayList<GWTJahiaCheckedLink>());
    }

    private static class InstanceHolder {
        private static final LinkChecker M_INSTANCE = new LinkChecker();
    }

    public static LinkChecker getInstance() {
        return InstanceHolder.M_INSTANCE;
    }

    public void purge() {
        m_links.clear();
    }

    public void add(GWTJahiaCheckedLink ... links) {
        for (GWTJahiaCheckedLink link: links) {
            m_links.add(link);
        }
    }

    public List<GWTJahiaCheckedLink> getLinks() {
        ArrayList<GWTJahiaCheckedLink> links = new ArrayList<GWTJahiaCheckedLink>(m_links.size());
        for (GWTJahiaCheckedLink link: m_links) {
            links.add(link);
        }
        purge();
        return links;
    }

    /**
     * This is the emulation of a link check in multiple phases.
     */
    public void startCheckingLinks() {
        new Thread() {
            public void run() {
                final String link = "http://www.link.com/";
                final String title = "title";
                final String url = "#";
                final int code = 404;
                int i = 0;
                running = true;
                purge();
                while (running) {
                    m_links.add(new GWTJahiaCheckedLink(link + i, title + i, url, code));
                    i++;
                    try {
                        long sleepTime = Math.round(Math.random() * 1000);
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        logger.error(e.toString(), e);
                    }
                }
            }
        }.start();
    }

    public void stopCheckingLinks() {
        running = false;
    }

}
