package org.jahia.rss;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.utils.RSSHelper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 5, 2010
 * Time: 4:07:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSUtil {

    private static final transient Logger logger = Logger.getLogger(RSSHelper.class);

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
           logger.error(e,e);
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


