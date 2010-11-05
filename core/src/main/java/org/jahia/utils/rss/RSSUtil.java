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

package org.jahia.utils.rss;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 6, 2010
 * Time: 3:59:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSUtil {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(RSSUtil.class);

    /**
     * Get a SyndFeed from an url
     * @param url
     * @return
     */
    public static SyndFeed loadSyndFeed(String url){
        try {
            //load corresponding url
            return loadSyndFeed(new URL(url));

        } catch (MalformedURLException e) {
           logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Get a SyndFeed from an URL
     * @param feedUrl
     * @return
     */
    public static SyndFeed loadSyndFeed(URL feedUrl) {
        boolean reformatGoogleNewsEntry = false;
        SyndFeed feed = null;
        try {
            final SyndFeedInput input = new SyndFeedInput();
            final XmlReader reader = new XmlReader(feedUrl.openStream());
            feed = input.build(reader);
        } catch (FeedException e) {
            logger.error(e.getMessage() + ", feedException --> Can't load rss.");
        } catch (IOException e) {
            // another way to load feed RSS
            final FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
            final FeedFetcher fetcher = new HttpURLFeedFetcher(feedInfoCache);
            try {
                feed = fetcher.retrieveFeed(feedUrl);
            } catch (Exception e1) {
                logger.error(e.getMessage() + ", exception --> Can't load rss.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage() + ", exception --> Can't load rss.");
        }
        if (feed == null) {
            return null;
        }

        // handle goole news
        if (feedUrl != null && "news.google.com".equals(feedUrl.getHost())) {
            reformatGoogleNewsEntry = true;
        }
        return feed;

    }


    /**
     * Reaformat codes coming from google news
     *
     * @param entry
     */
    public static void reformatGoogleNewsEntry(SyndEntry entry) {
        final SyndContent entryDescription = entry.getDescription();
        if (entryDescription != null) {
            final String description = entryDescription.getValue();
            final int descBegin = description.lastIndexOf("<font size=-1>") + 14;
            final int descEnd = description.indexOf("</font>", descBegin);

            if (descBegin != -1 && descEnd != -1) {
                final String realDesc = description.substring(descBegin, descEnd);
                entryDescription.setValue(realDesc);
            }
        }
    }
}



