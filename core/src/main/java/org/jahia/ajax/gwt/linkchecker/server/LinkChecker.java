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

import static org.apache.commons.httpclient.HttpStatus.*;

import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaLinkCheckerStatus;
import org.jahia.services.integrity.Link;
import org.jahia.services.integrity.LinkValidationResult;
import org.jahia.services.integrity.LinkValidatorService;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 15:26:28
 */
class LinkChecker {

    private final static Logger logger = Logger.getLogger(LinkChecker.class);

    private int batchSize;
    private LinkValidatorService linkValidatorService; 
    private List<Object[]> m_links = new LinkedList<Object[]>(); 
    private boolean running = false;
    private GWTJahiaLinkCheckerStatus status = new GWTJahiaLinkCheckerStatus();
    

    public void purge() {
        m_links.clear();
    }

    public void add(Link link, LinkValidationResult result) {
        m_links.add(new Object[] {link, result});
    }

    public List<Object[]> getLinks() {
        List<Object[]> links = new LinkedList<Object[]>(m_links);
        purge();
        return links;
    }

    /**
     * This is the emulation of a link check in multiple phases.
     * @param siteId the ID of the site to check links
     */
    public void startCheckingLinks(final int siteId) {
        if (running) {
            logger.warn("Link validation process is already running");
            return;
        }
        new Thread() {
            public void run() {
                running = true;
                status = new GWTJahiaLinkCheckerStatus();
                purge();
                status.setActive(true);
                
                List<Link> links = linkValidatorService.getAllLinks(siteId);
                status.setTotal(links.size());
                
                while (running && !links.isEmpty()) {
                    int toIndex = Math.min(links.size(), batchSize);
                    List<Link> toProcess = links.subList(0, toIndex);
                    Map<Link, LinkValidationResult> validationResult = linkValidatorService.validate(toProcess);
                    for (Map.Entry<Link, LinkValidationResult> entry : validationResult.entrySet()) {
                        status.setProcessed(status.getProcessed()+1);
                        if (entry.getValue().getErrorCode() == SC_OK) {
                            status.setSuccess(status.getSuccess()+1);
                        } else {
                            status.setFailed(status.getFailed()+1);
                            add(entry.getKey(), entry.getValue());
                        }
                    }
                    toProcess.clear();
                }
                status.setActive(false);
                running = false;
            }
        }.start();
    }

    public void stopCheckingLinks() {
        if (!running) {
            logger.warn("Link validation process is not running");
        }
        running = false;
    }

    /**
     * Returns the link validation process status, including total count and
     * remaining links.
     * 
     * @return the link validation process status, including total count and
     *         remaining links
     */
    public GWTJahiaLinkCheckerStatus getStatus() {
        return status;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setLinkValidatorService(LinkValidatorService linkValidatorService) {
        this.linkValidatorService = linkValidatorService;
    }

}
