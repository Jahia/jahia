<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:beans="http://www.springframework.org/schema/beans"
                xmlns="http://www.springframework.org/schema/beans">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="beans:property/@ref[.='sessionFactory']">
        <xsl:attribute name="ref">
            <xsl:value-of select="'moduleSessionFactory'"/>
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="beans:beans">
        <beans>
            <xsl:apply-templates select="@* | *"/>

            <bean id="moduleSessionFactory"
                  class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean"
                  depends-on="settingsBean">
                <property name="dataSource">
                    <ref bean="dataSource"/>
                </property>
                <property name="packagesToScan" value="org.jahia.modules"/>

                <property name="hibernateProperties">
                    <bean class="org.springframework.beans.factory.config.PropertiesFactoryBean">
                        <property name="properties">
                            <map>
                                <entry key="hibernate.dialect" value="${{hibernate.dialect}}"/>
                                <entry key="hibernate.hbm2ddl.auto" value="none"/>
                                <entry key="hibernate.show_sql" value="false"/>
                                <entry key="hibernate.cache.provider_class"
                                       value="net.sf.ehcache.hibernate.SharingEhcacheProvider"/>
                                <entry key="hibernate.cache.use_second_level_cache" value="true"/>
                                <!-- If you use an invalidation-based cache, make sure you turn the query cache off ! -->
                                <entry key="hibernate.cache.use_query_cache" value="true"/>
                                <!-- put the batch size property to 0 if you need to debug -->
                                <entry key="hibernate.jdbc.batch_size" value="16"/>
                                <entry key="hibernate.default_batch_fetch_size" value="16"/>
                                <entry key="hibernate.max_fetch_depth" value="3"/>
                                <entry key="hibernate.query.substitutions" value="true 1, false 0"/>
                                <entry key="hibernate.generate_statistics" value="false"/>
                                <entry key="hibernate.cache.use_structured_entries" value="false"/>
                                <entry key="org.jahia.hibernate.ehcache.existingCacheManagerName" value="org.jahia.hibernate.ehcachemanager">
                                </entry>
                            </map>
                        </property>
                    </bean>
                </property>
                <property name="eventListeners">
                    <map>
                        <entry key="merge">
                            <bean class="org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener"/>
                        </entry>
                    </map>
                </property>
                <property name="configurationClass" value="org.hibernate.cfg.AnnotationConfiguration"/>
            </bean>

        </beans>

    </xsl:template>

</xsl:stylesheet>