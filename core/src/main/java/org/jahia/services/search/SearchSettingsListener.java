package org.jahia.services.search;

import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.ExternalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * Created by kevan on 21/07/14.
 */
public class SearchSettingsListener extends DefaultEventListener implements ExternalEventListener,
        ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(SearchSettingsListener.class);

    private ApplicationEventPublisher applicationEventPublisher;


    @Override
    public int getEventTypes() {
        return Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED;
    }

    @Override
    public String getPath() {
        return "/settings/search-settings";
    }

    public void onEvent(EventIterator events) {
        boolean external = false;
        while (events.hasNext()) {
            if (isExternal(events.nextEvent())) {
                external = true;
                break;
            }
        }
        if (!external) {
            return;
        }

        logger.info("Event received about changes in search server settings."
                + " Notifying search service...");
        applicationEventPublisher.publishEvent(new SearchServiceImpl.SearchSettingsChangedEvent(this));
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
