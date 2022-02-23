/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.springframework.orm.hibernate5.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.DeferredResultProcessingInterceptor;

import java.util.concurrent.Callable;

/**
 * An interceptor with asynchronous web requests used in OpenSessionInViewFilter and
 * OpenSessionInViewInterceptor.
 * <p>
 * Ensures the following:
 * 1) The session is bound/unbound when "callable processing" is started
 * 2) The session is closed if an async request times out or an error occurred
 *
 * @author Rossen Stoyanchev
 * @since 4.2
 */
class AsyncRequestInterceptor extends CallableProcessingInterceptorAdapter implements DeferredResultProcessingInterceptor {

    private static final Log logger = LogFactory.getLog(AsyncRequestInterceptor.class);

    private final SessionFactory sessionFactory;

    private final SessionHolder sessionHolder;

    private volatile boolean timeoutInProgress;

    public AsyncRequestInterceptor(SessionFactory sessionFactory, SessionHolder sessionHolder) {
        this.sessionFactory = sessionFactory;
        this.sessionHolder = sessionHolder;
    }

    @Override
    public <T> void preProcess(NativeWebRequest request, Callable<T> task) {
        bindSession();
    }

    public void bindSession() {
        this.timeoutInProgress = false;
        TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sessionHolder);
    }

    @Override
    public <T> void postProcess(NativeWebRequest request, Callable<T> task, Object concurrentResult) {
        TransactionSynchronizationManager.unbindResource(this.sessionFactory);
    }

    @Override
    public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) {
        this.timeoutInProgress = true;
        return RESULT_NONE;  // give other interceptors a chance to handle the timeout
    }

    @Override
    public <T> void afterCompletion(NativeWebRequest request, Callable<T> task) throws Exception {
        closeSession();
    }

    private void closeSession() {
        if (this.timeoutInProgress) {
            logger.debug("Closing Hibernate Session after async request timeout/error");
            SessionFactoryUtils.closeSession(this.sessionHolder.getSession());
        }
    }

    public <T> void beforeConcurrentHandling(NativeWebRequest request, DeferredResult<T> deferredResult) {
    }

    @Override
    public <T> void preProcess(NativeWebRequest nativeWebRequest, DeferredResult<T> deferredResult) throws Exception {
    }

    @Override
    public <T> void postProcess(NativeWebRequest nativeWebRequest, DeferredResult<T> deferredResult, Object o) throws Exception {
    }

    // Implementation of DeferredResultProcessingInterceptor methods

    @Override
    public <T> boolean handleTimeout(NativeWebRequest request, DeferredResult<T> deferredResult) {
        this.timeoutInProgress = true;
        return true;  // give other interceptors a chance to handle the timeout
    }

    @Override
    public <T> void afterCompletion(NativeWebRequest request, DeferredResult<T> deferredResult) {
        closeSession();
    }

}
