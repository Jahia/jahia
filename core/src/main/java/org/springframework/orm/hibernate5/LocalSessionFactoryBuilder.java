/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.springframework.orm.hibernate5;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.resource.jdbc.spi.PhysicalConnectionHandlingMode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.InfrastructureProxy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.persistence.*;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A Spring-provided extension of the standard Hibernate {@link Configuration} class,
 * adding {@link SpringSessionContext} as a default and providing convenient ways
 * to specify a JDBC {@link DataSource} and an application class loader.
 *
 * <p>This is designed for programmatic use, e.g. in {@code @Bean} factory methods;
 * consider using {@link LocalSessionFactoryBean} for XML bean definition files.
 * Typically combined with {@link HibernateTransactionManager} for declarative
 * transactions against the {@code SessionFactory} and its JDBC {@code DataSource}.
 *
 * <p>Compatible with Hibernate 5.2/5.3/5.4, as of Spring 5.3.
 * This Hibernate-specific factory builder can also be a convenient way to set up
 * a JPA {@code EntityManagerFactory} since the Hibernate {@code SessionFactory}
 * natively exposes the JPA {@code EntityManagerFactory} interface as well now.
 *
 * <p>This builder supports Hibernate 5.3/5.4 {@code BeanContainer} integration,
 * {@link MetadataSources} from custom {@link BootstrapServiceRegistryBuilder}
 * setup, as well as other advanced Hibernate configuration options beyond the
 * standard JPA bootstrap contract.
 *
 * @author Juergen Hoeller
 * @see HibernateTransactionManager
 * @see LocalSessionFactoryBean
 * @see #setBeanContainer
 * @see #LocalSessionFactoryBuilder(DataSource, ResourceLoader, MetadataSources)
 * @see BootstrapServiceRegistryBuilder
 * @since 4.2
 */
@SuppressWarnings("serial")
public class LocalSessionFactoryBuilder extends Configuration {

    private static final String RESOURCE_PATTERN = "/**/*.class";

    private static final String PACKAGE_INFO_SUFFIX = ".package-info";

    private static final TypeFilter[] DEFAULT_ENTITY_TYPE_FILTERS = new TypeFilter[]{
            new AnnotationTypeFilter(Entity.class, false),
            new AnnotationTypeFilter(Embeddable.class, false),
            new AnnotationTypeFilter(MappedSuperclass.class, false)};

    private static final TypeFilter CONVERTER_TYPE_FILTER = new AnnotationTypeFilter(Converter.class, false);


    private final ResourcePatternResolver resourcePatternResolver;

    private TypeFilter[] entityTypeFilters = DEFAULT_ENTITY_TYPE_FILTERS;


    /**
     * Create a new LocalSessionFactoryBuilder for the given DataSource.
     *
     * @param dataSource the JDBC DataSource that the resulting Hibernate SessionFactory should be using
     *                   (may be {@code null})
     */
    public LocalSessionFactoryBuilder(DataSource dataSource) {
        this(dataSource, new PathMatchingResourcePatternResolver());
    }

    /**
     * Create a new LocalSessionFactoryBuilder for the given DataSource.
     *
     * @param dataSource  the JDBC DataSource that the resulting Hibernate SessionFactory should be using
     *                    (may be {@code null})
     * @param classLoader the ClassLoader to load application classes from
     */
    public LocalSessionFactoryBuilder(DataSource dataSource, ClassLoader classLoader) {
        this(dataSource, new PathMatchingResourcePatternResolver(classLoader));
    }

    /**
     * Create a new LocalSessionFactoryBuilder for the given DataSource.
     *
     * @param dataSource     the JDBC DataSource that the resulting Hibernate SessionFactory should be using
     *                       (may be {@code null})
     * @param resourceLoader the ResourceLoader to load application classes from
     */
    public LocalSessionFactoryBuilder(DataSource dataSource, ResourceLoader resourceLoader) {
        this(dataSource, resourceLoader, new MetadataSources(
                new BootstrapServiceRegistryBuilder().applyClassLoader(resourceLoader.getClassLoader()).build()));
    }

    /**
     * Create a new LocalSessionFactoryBuilder for the given DataSource.
     *
     * @param dataSource      the JDBC DataSource that the resulting Hibernate SessionFactory should be using
     *                        (may be {@code null})
     * @param resourceLoader  the ResourceLoader to load application classes from
     * @param metadataSources the Hibernate MetadataSources service to use (e.g. reusing an existing one)
     * @since 4.3
     */
    public LocalSessionFactoryBuilder(
            DataSource dataSource, ResourceLoader resourceLoader, MetadataSources metadataSources) {

        super(metadataSources);

        getProperties().put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, SpringSessionContext.class.getName());
        if (dataSource != null) {
            getProperties().put(AvailableSettings.DATASOURCE, dataSource);
        }
        getProperties().put(AvailableSettings.CONNECTION_HANDLING,
                PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_HOLD);

        getProperties().put(AvailableSettings.CLASSLOADERS, Collections.singleton(resourceLoader.getClassLoader()));
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }


    /**
     * Set the Spring {@link JtaTransactionManager} or the JTA {@link TransactionManager}
     * to be used with Hibernate, if any. Allows for using a Spring-managed transaction
     * manager for Hibernate 5's session and cache synchronization, with the
     * "hibernate.transaction.jta.platform" automatically set to it.
     * <p>A passed-in Spring {@link JtaTransactionManager} needs to contain a JTA
     * {@link TransactionManager} reference to be usable here, except for the WebSphere
     * case where we'll automatically set {@code WebSphereExtendedJtaPlatform} accordingly.
     * <p>Note: If this is set, the Hibernate settings should not contain a JTA platform
     * setting to avoid meaningless double configuration.
     */
    public LocalSessionFactoryBuilder setJtaTransactionManager(Object jtaTransactionManager) {
        Assert.notNull(jtaTransactionManager, "Transaction manager reference must not be null");

        if (jtaTransactionManager instanceof JtaTransactionManager) {
            boolean webspherePresent = ClassUtils.isPresent("com.ibm.wsspi.uow.UOWManager", getClass().getClassLoader());
            if (webspherePresent) {
                getProperties().put(AvailableSettings.JTA_PLATFORM,
                        "org.hibernate.engine.transaction.jta.platform.internal.WebSphereExtendedJtaPlatform");
            } else {
                JtaTransactionManager jtaTm = (JtaTransactionManager) jtaTransactionManager;
                if (jtaTm.getTransactionManager() == null) {
                    throw new IllegalArgumentException(
                            "Can only apply JtaTransactionManager which has a TransactionManager reference set");
                }
                try {
                    Field f = jtaTm.getClass().getDeclaredField("transactionSynchronizationRegistry");
                    f.setAccessible(true);

                    getProperties().put(AvailableSettings.JTA_PLATFORM,
                            new ConfigurableJtaPlatform(jtaTm.getTransactionManager(), jtaTm.getUserTransaction(),
                                    (TransactionSynchronizationRegistry) f.get(jtaTm)));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new IllegalArgumentException(
                            "Unknown transaction manager type: " + jtaTransactionManager.getClass().getName(), e);
                }
            }
        } else if (jtaTransactionManager instanceof TransactionManager) {
            getProperties().put(AvailableSettings.JTA_PLATFORM,
                    new ConfigurableJtaPlatform((TransactionManager) jtaTransactionManager, null, null));
        } else {
            throw new IllegalArgumentException(
                    "Unknown transaction manager type: " + jtaTransactionManager.getClass().getName());
        }

        getProperties().put(AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY, "jta");
        getProperties().put(AvailableSettings.CONNECTION_HANDLING,
                PhysicalConnectionHandlingMode.DELAYED_ACQUISITION_AND_RELEASE_AFTER_STATEMENT);

        return this;
    }

    /**
     * Set a Hibernate {@link org.hibernate.resource.beans.container.spi.BeanContainer}
     * for the given Spring {@link ConfigurableListableBeanFactory}.
     * <p>Note: Bean container integration requires Hibernate 5.3 or higher.
     * It enables autowiring of Hibernate attribute converters and entity listeners.
     *
     * @see SpringBeanContainer
     * @see AvailableSettings#BEAN_CONTAINER
     * @since 5.1
     */
    public LocalSessionFactoryBuilder setBeanContainer(ConfigurableListableBeanFactory beanFactory) {
        getProperties().put(AvailableSettings.BEAN_CONTAINER, new SpringBeanContainer(beanFactory));
        return this;
    }

    /**
     * Set the Hibernate {@link RegionFactory} to use for the SessionFactory.
     * Allows for using a Spring-managed {@code RegionFactory} instance.
     * <p>Note: If this is set, the Hibernate settings should not define a
     * cache provider to avoid meaningless double configuration.
     *
     * @see AvailableSettings#CACHE_REGION_FACTORY
     * @since 5.1
     */
    public LocalSessionFactoryBuilder setCacheRegionFactory(RegionFactory cacheRegionFactory) {
        getProperties().put(AvailableSettings.CACHE_REGION_FACTORY, cacheRegionFactory);
        return this;
    }

    /**
     * Set a {@link MultiTenantConnectionProvider} to be passed on to the SessionFactory.
     *
     * @see AvailableSettings#MULTI_TENANT_CONNECTION_PROVIDER
     * @since 4.3
     */
    public LocalSessionFactoryBuilder setMultiTenantConnectionProvider(MultiTenantConnectionProvider multiTenantConnectionProvider) {
        getProperties().put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        return this;
    }

    /**
     * Overridden to reliably pass a {@link CurrentTenantIdentifierResolver} to the SessionFactory.
     *
     * @see AvailableSettings#MULTI_TENANT_IDENTIFIER_RESOLVER
     * @since 4.3.2
     */
    @Override
    public void setCurrentTenantIdentifierResolver(CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {
        getProperties().put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolver);
        super.setCurrentTenantIdentifierResolver(currentTenantIdentifierResolver);
    }

    /**
     * Specify custom type filters for Spring-based scanning for entity classes.
     * <p>Default is to search all specified packages for classes annotated with
     * {@code @javax.persistence.Entity}, {@code @javax.persistence.Embeddable}
     * or {@code @javax.persistence.MappedSuperclass}.
     *
     * @see #scanPackages
     */
    public LocalSessionFactoryBuilder setEntityTypeFilters(TypeFilter... entityTypeFilters) {
        this.entityTypeFilters = entityTypeFilters;
        return this;
    }

    /**
     * Add the given annotated classes in a batch.
     *
     * @see #addAnnotatedClass
     * @see #scanPackages
     */
    public LocalSessionFactoryBuilder addAnnotatedClasses(Class<?>... annotatedClasses) {
        for (Class<?> annotatedClass : annotatedClasses) {
            addAnnotatedClass(annotatedClass);
        }
        return this;
    }

    /**
     * Add the given annotated packages in a batch.
     *
     * @see #addPackage
     * @see #scanPackages
     */
    public LocalSessionFactoryBuilder addPackages(String... annotatedPackages) {
        for (String annotatedPackage : annotatedPackages) {
            addPackage(annotatedPackage);
        }
        return this;
    }

    /**
     * Perform Spring-based scanning for entity classes, registering them
     * as annotated classes with this {@code Configuration}.
     *
     * @param packagesToScan one or more Java package names
     * @throws HibernateException if scanning fails for any reason
     */
    @SuppressWarnings("unchecked")
    public LocalSessionFactoryBuilder scanPackages(String... packagesToScan) throws HibernateException {
        Set<String> entityClassNames = new TreeSet<>();
        Set<String> converterClassNames = new TreeSet<>();
        Set<String> packageNames = new TreeSet<>();
        try {
            for (String pkg : packagesToScan) {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(pkg) + RESOURCE_PATTERN;
                Resource[] resources = this.resourcePatternResolver.getResources(pattern);
                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader reader = readerFactory.getMetadataReader(resource);
                        String className = reader.getClassMetadata().getClassName();
                        if (matchesEntityTypeFilter(reader, readerFactory)) {
                            entityClassNames.add(className);
                        } else if (CONVERTER_TYPE_FILTER.match(reader, readerFactory)) {
                            converterClassNames.add(className);
                        } else if (className.endsWith(PACKAGE_INFO_SUFFIX)) {
                            packageNames.add(className.substring(0, className.length() - PACKAGE_INFO_SUFFIX.length()));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new MappingException("Failed to scan classpath for unlisted classes", ex);
        }
        try {
            ClassLoader cl = this.resourcePatternResolver.getClassLoader();
            for (String className : entityClassNames) {
                addAnnotatedClass(ClassUtils.forName(className, cl));
            }
            for (String className : converterClassNames) {
                addAttributeConverter((Class<? extends AttributeConverter<?, ?>>) ClassUtils.forName(className, cl));
            }
            for (String packageName : packageNames) {
                addPackage(packageName);
            }
        } catch (ClassNotFoundException ex) {
            throw new MappingException("Failed to load annotated classes from classpath", ex);
        }
        return this;
    }

    /**
     * Check whether any of the configured entity type filters matches
     * the current class descriptor contained in the metadata reader.
     */
    private boolean matchesEntityTypeFilter(MetadataReader reader, MetadataReaderFactory readerFactory) throws IOException {
        if (this.entityTypeFilters != null) {
            for (TypeFilter filter : this.entityTypeFilters) {
                if (filter.match(reader, readerFactory)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Build the Hibernate {@code SessionFactory} through background bootstrapping,
     * using the given executor for a parallel initialization phase
     * (e.g. a {@link org.springframework.core.task.SimpleAsyncTaskExecutor}).
     * <p>{@code SessionFactory} initialization will then switch into background
     * bootstrap mode, with a {@code SessionFactory} proxy immediately returned for
     * injection purposes instead of waiting for Hibernate's bootstrapping to complete.
     * However, note that the first actual call to a {@code SessionFactory} method will
     * then block until Hibernate's bootstrapping completed, if not ready by then.
     * For maximum benefit, make sure to avoid early {@code SessionFactory} calls
     * in init methods of related beans, even for metadata introspection purposes.
     *
     * @see #buildSessionFactory()
     * @since 4.3
     */
    public SessionFactory buildSessionFactory(AsyncTaskExecutor bootstrapExecutor) {
        Assert.notNull(bootstrapExecutor, "AsyncTaskExecutor must not be null");
        return (SessionFactory) Proxy.newProxyInstance(this.resourcePatternResolver.getClassLoader(),
                new Class<?>[]{SessionFactoryImplementor.class, InfrastructureProxy.class},
                new BootstrapSessionFactoryInvocationHandler(bootstrapExecutor));
    }


    /**
     * Proxy invocation handler for background bootstrapping, only enforcing
     * a fully initialized target {@code SessionFactory} when actually needed.
     *
     * @since 4.3
     */
    private class BootstrapSessionFactoryInvocationHandler implements InvocationHandler {

        private final Future<SessionFactory> sessionFactoryFuture;

        public BootstrapSessionFactoryInvocationHandler(AsyncTaskExecutor bootstrapExecutor) {
            this.sessionFactoryFuture = bootstrapExecutor.submit(
                    (Callable<SessionFactory>) LocalSessionFactoryBuilder.this::buildSessionFactory);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "equals":
                    // Only consider equal when proxies are identical.
                    return (proxy == args[0]);
                case "hashCode":
                    // Use hashCode of EntityManagerFactory proxy.
                    return System.identityHashCode(proxy);
                case "getProperties":
                    return getProperties();
                case "getWrappedObject":
                    // Call coming in through InfrastructureProxy interface...
                    return getSessionFactory();
            }

            // Regular delegation to the target SessionFactory,
            // enforcing its full initialization...
            try {
                return method.invoke(getSessionFactory(), args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }

        private SessionFactory getSessionFactory() {
            try {
                return this.sessionFactoryFuture.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted during initialization of Hibernate SessionFactory", ex);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof HibernateException) {
                    // Rethrow a provider configuration exception (possibly with a nested cause) directly
                    throw (HibernateException) cause;
                }
                throw new IllegalStateException("Failed to asynchronously initialize Hibernate SessionFactory: " +
                        ex.getMessage(), cause);
            }
        }
    }

}
