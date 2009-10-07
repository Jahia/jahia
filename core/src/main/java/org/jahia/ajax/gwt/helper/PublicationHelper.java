package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:15:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationHelper {
    private static Logger logger = Logger.getLogger(PublicationHelper.class);

    private JCRSessionFactory sessionFactory;
    private JCRPublicationService publicationService ;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * Get the publication status information for a particular path.
     *
     * @param path to get publication info from
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public GWTJahiaPublicationInfo getPublicationInfo(String path, Set<String> languages, boolean includesReferences) throws GWTJahiaServiceException {
        try {
            PublicationInfo pubInfo = publicationService.getPublicationInfo(path, languages, includesReferences);
            return convert(path, pubInfo);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    public GWTJahiaPublicationInfo convert(String path, PublicationInfo pubInfo) {
        List<GWTJahiaPublicationInfo> list = new ArrayList<GWTJahiaPublicationInfo>();

        GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(path, pubInfo.getStatus(), pubInfo.isCanPublish());
        for (String p : pubInfo.getReferences().keySet()) {
            PublicationInfo pi = pubInfo.getReferences().get(p);
            gwtInfo.add(convert(p, pi));
        }

        return gwtInfo;
    }

    /**
     * Publish a node into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param path          Path of the node to publish
     * @param languages     Set of languages to publish if null publish all languages
     * @param user          the user for obtaining the jcr session
     * @param publishParent Recursively publish the parents
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void publish(String path, Set<String> languages, JahiaUser user, boolean publishParent) throws GWTJahiaServiceException {
        try {
            publicationService.publish(path, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, publishParent, false);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Publish a list of nodes into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param paths         list of paths of the nodes to publish
     * @param languages     Set of languages to publish if null publish all languages
     * @param user          the user for obtaining the jcr session
     * @param publishParent Recursively publish the parents
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void publish(List<String> paths, Set<String> languages, JahiaUser user, boolean publishParent) throws GWTJahiaServiceException {
        // TODO implement me!
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param path      path of the node to unpublish
     * @param languages Set of languages to unpublish if null unpublish all languages
     * @param user      the user for obtaining the jcr session
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void unpublish(String path, Set<String> languages, JahiaUser user) throws GWTJahiaServiceException {
        try {
            publicationService.unpublish(path, languages);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }
}
