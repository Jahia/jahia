/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.osgi.spring;

import java.util.Collection;
import java.util.Map;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;

/**
 * Exposes a configurable set of Spring beans as an OSGi services.
 */
public class SpringBridge implements JahiaAfterInitializationService {

    /**
     * Selects a set of beans from the application context, to expose them as an OSGi services.
     */
    public interface BeanSelector {

        /**
         * Select a set of beans from the application context, to expose them as an OSGi services.
         *
         * @param applicationContext Spring application context
         * @return A map of beans to be exposed as an OSGi services, by their IDs
         */
        Map<String, Object> selectBeans(ApplicationContext applicationContext);
    }

    /**
     * Exposes Spring beans as an OSGi services.
     *
     * Implementations may expose a bean as its class, as an interface implemented by the bean, etc.
     */
    public interface BeanExposer {

        /**
         * Expose the bean as an OSGi service.
         *
         * @param beanID        Spring bean ID
         * @param bean          Spring bean to expose as an OSGi service
         * @param bundleContext OSGi bundle context
         */
        void exposeBean(String beanID, Object bean, BundleContext bundleContext);
    }

    /**
     * Incapsulates bean exposition rules: which beans to expose, and how to expose.
     */
    public static class BeanExposition {

        private BeanSelector selector;
        private BeanExposer exposer;

        /**
         * @return Selector of beans to expose as an OSGi services
         */
        public BeanSelector getSelector() {
            return selector;
        }

        /**
         * @param selector Selector of beans to expose as an OSGi services
         */
        public void setSelector(BeanSelector selector) {
            this.selector = selector;
        }

        /**
         * @return Exposer of beans selected
         */
        public BeanExposer getExposer() {
            return exposer;
        }

        /**
         * @param exposer Exposer of beans selected
         */
        public void setExposer(BeanExposer exposer) {
            this.exposer = exposer;
        }
    }

    private Collection<BeanExposition> beanExpositions;

    /**
     * @param beanExpositions A set of bean exposition rules, so that various beans can be exposed in various ways.
     */
    public void setBeanExpositions(Collection<BeanExposition> beanExpositions) {
        this.beanExpositions = beanExpositions;
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        exposeSpringBeans();
    }

    private void exposeSpringBeans() {
        ApplicationContext applicationContext = SpringContextSingleton.getInstance().getContext();
        BundleContext bundleContext = FrameworkService.getBundleContext();
        for (BeanExposition beanExposition : beanExpositions) {
            Map<String, Object> beansByID = beanExposition.getSelector().selectBeans(applicationContext);
            BeanExposer beanExposer = beanExposition.getExposer();
            for (Map.Entry<String, Object> beanEntry : beansByID.entrySet()) {
                String beanID = beanEntry.getKey();
                Object bean = beanEntry.getValue();
                beanExposer.exposeBean(beanID, bean, bundleContext);
            }
        }
    }
}
