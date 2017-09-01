package org.jahia.services.content;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;

/**
 * Content publication event.
 */
public interface PublicationEvent {

    /**
     * @return Timestamp of publication completion.
     */
    long getTimestamp();

    /**
     * @return Source JCR session of the publication.
     */
    JCRSessionWrapper getSourceSession();

    /**
     * @return Destination JCR session of the publication.
     */
    JCRSessionWrapper getDestinationSession();

    /**
     * @return Information about published contents.
     */
    Collection<ContentPublicationInfo> getContentPublicationInfos();

    /**
     * Information about a piece of published content.
     * <p>
     * Two instances are considered equal if they have the same content node UUID and publication languages.
     */
    public static class ContentPublicationInfo {

        private String nodeIdentifier;
        private String nodePath;
        private String nodeType;
        private Collection<String> publicationLanguages;

        /**
         * Create a published content info instance.
         *
         * @param nodeIdentifier UUID of the content node
         * @param nodePath Path of the content node
         * @param nodeType Primary type of the content node
         * @param publicationLanguages Publication languages
         */
        public ContentPublicationInfo(String nodeIdentifier, String nodePath, String nodeType, Collection<String> publicationLanguages) {
            this.nodeIdentifier = nodeIdentifier;
            this.nodePath = nodePath;
            this.nodeType = nodeType;
            if (publicationLanguages == null || publicationLanguages.isEmpty()) {
                this.publicationLanguages = null;
            } else {
                this.publicationLanguages = Collections.unmodifiableCollection(publicationLanguages);
            }
        }

        /**
         * @return UUID of the content node
         */
        public String getNodeIdentifier() {
            return nodeIdentifier;
        }

        /**
         * @return Path of the content node
         */
        public String getNodePath() {
            return nodePath;
        }

        /**
         * @return Primary type of the content node
         */
        public String getNodeType() {
            return nodeType;
        }

        /**
         * @return Publication languages, or null if unavailable
         */
        public Collection<String> getPublicationLanguages() {
            return publicationLanguages;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((nodeIdentifier == null) ? 0 : nodeIdentifier.hashCode());
            result = prime * result + ((publicationLanguages == null) ? 0 : getPublicationLanguagesString().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ContentPublicationInfo other = (ContentPublicationInfo) obj;
            if (nodeIdentifier == null) {
                if (other.nodeIdentifier != null) {
                    return false;
                }
            } else if (!nodeIdentifier.equals(other.nodeIdentifier)) {
                return false;
            }
            if (publicationLanguages == null) {
                if (other.publicationLanguages != null) {
                    return false;
                }
            } else if (!getPublicationLanguagesString().equals(other.getPublicationLanguagesString())) {
                return false;
            }
            return true;
        }

        private String getPublicationLanguagesString() {
            return StringUtils.join(publicationLanguages, ',');
        }
    }
}
