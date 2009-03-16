package org.jahia.data.events;

import org.jahia.registries.ServicesRegistry;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 5 mars 2009
 * Time: 10:12:22
 *
 * @author Ibrahim El Ghandour
 * 
 */
public class JAMonEventListener extends JahiaEventListener {
    private static Logger logger = Logger.getLogger(JAMonEventListener.class);

    /*SITE = 1*/
    @Override
    public void siteAdded(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 1, "Added");
        }
    }

    @Override
    public void siteDeleted(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 1, "deleted");
        }
    }

    /*----------------------------------------------------------------------------------------------*/
    /*FIELD = 2*/
    @Override
    public void fieldAdded(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 2, "added");
        }
    }

    @Override
    public void fieldUpdated(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 2, "updated");
        }
    }

    @Override
    public void fieldDeleted(JahiaEvent je) {
        if (je != null && je.getObject() != null ) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 2, "deleted");
        }
    }

    /*----------------------------------------------------------------------------------------------*/
    /*CONTAINER = 3*/
    @Override
    public void containerAdded(JahiaEvent je) {
        if (je != null && je.getObject() != null ) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 3, "added");
        }
        //logger.info("JAMonEventListener container added");
    }

    @Override
    public void containerUpdated(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 3, "updated");
        }
        //logger.info("JAMonEventListener container updated");
    }

    @Override
    public void containerDeleted(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 3, "deleted");
        }
        //logger.info("JAMonEventListener container deleted");
    }

    @Override
    public void containerListPropertiesSet(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 10, "Container properties set");
        }
        //logger.info("JAMonEventListener containerListPropertiesSet");
    }

    @Override
    public void containerValidation(JahiaEvent je) {
        if (je != null && je.getObject() != null ) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 3, "validated");
        }
    }

    /*----------------------------------------------------------------------------------------------*/
    /*PAGE = 4*/
    @Override
    public void pageAdded(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 4, "added");
        }
        //logger.info("JAMonEventListener page added");
    }

    @Override
    public void pageLoaded(JahiaEvent je) {
        if (je != null && je.getObject() != null && !je.getProcessingContext().getTheUser().getUsername().equals("guest")) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 4, "loaded");
        }
        //logger.info("JAMonEventListener page loaded");
    }

    @Override
    public void pageDeleted(JahiaEvent je) {
        if (je != null && je.getObject() != null ) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 4, "deleted");
        }
    }

    @Override
    public void pageAccepted(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 4, "accepted");
        }
    }

    @Override
    public void pageRejected(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 4, "rejected");
        }
    }

    @Override
    public void pagePropertiesSet(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 4, "Page Properties set");
        }
        //logger.info("JAMonEventListener pagePropertiesSet");
    }

    /*----------------------------------------------------------------------------------------------*/
    /*USER = 5*/
    @Override
    public void userAdded(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 5, "added");
        }
    }

    @Override
    public void userDeleted(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 5, "deleted");
        }
    }

    @Override
    public void userUpdated(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 5, "updated");
        }
    }

    @Override
    public void userPropertiesSet(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je,5, "user properties set");
        }
    }

    @Override
    public void userLoggedIn(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 5, "logged in");
        }
    }

    @Override
    public void userLoggedOut(JahiaEvent je) {
        if (je != null && je.getObject() != null ) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 5, "logged out");
        }
    }

    /*----------------------------------------------------------------------------------------------*/
    /*TEMPLATE = 6*/
    @Override
    public void templateUpdated(JahiaEvent je) {
        if (je != null && je.getObject() != null ) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 6, "updated");
        }
    }

    @Override
    public void templateAdded(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 6, "added");
        }
    }

    @Override
    public void templateDeleted(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 6, "deleted");
        }
    }

    /*----------------------------------------------------------------------------------------------*/
    /*CATEGORY = 7*/
    @Override
    public void categoryUpdated(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 7, "updated");
        }
    }

    /*----------------------------------------------------------------------------------------------*/
    /*GROUP = 8*/
    @Override
    public void groupDeleted(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 8, "deleted");
        }
    }

    @Override
    public void groupAdded(JahiaEvent je) {
        if (je != null && je.getObject() != null ) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 8, "added");
        }
    }

    @Override
    public void groupUpdated(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 8, "updated");
        }
    }

    /*----------------------------------------------------------------------------------------------*/
    /*RIGHTS = 9*/
    @Override
    public void rightsSet(JahiaEvent je) {
        if (je != null && je.getObject() != null) {
            logger.info("----- > rights set");
            ServicesRegistry.getInstance().getAnalyticsService().trackEvent(je, 9, "rights set");
        }
    }
    /*----------------------------------------------------------------------------------------------*/
}
