package org.jahia.services.content;

/**
 * Listener of content publication events.
 */
public interface PublicationEventListener {

    /**
     * Invoked when publication of a batch of nodes is successfully completed.
     *
     * @param publicationEvent Publication info
     */
    void onPublicationCompleted(PublicationEvent publicationEvent);
}
