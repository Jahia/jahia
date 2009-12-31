package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.authentication.AuthContext;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

/**
 * Jackrabbit repository extension
 *
 * Used to return session extension
 *
 */
public class JahiaRepositoryImpl extends RepositoryImpl {
    public JahiaRepositoryImpl(RepositoryConfig repConfig) throws RepositoryException {
        super(repConfig);
    }

    /**
     * Creates a new <code>RepositoryImpl</code> instance.
     * <p/>
     *
     * @param config the configuration of the repository
     * @return a new <code>RepositoryImpl</code> instance
     * @throws RepositoryException If an error occurs
     */
    public static RepositoryImpl create(RepositoryConfig config)
            throws RepositoryException {
        return new JahiaRepositoryImpl(config);
    }

    @Override
    protected SessionImpl createSessionInstance(AuthContext loginContext, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        return new JahiaSessionImpl(this, loginContext, wspConfig);
    }

    @Override
    protected SessionImpl createSessionInstance(Subject subject, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        return new JahiaSessionImpl(this, subject, wspConfig);
    }
}
