package org.tails.bpm.test.conf;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;


@Configuration
@EnableTransactionManagement
public class DatabaseConfiguration {

    private static final Logger LOG =
        LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        LOG.info("Configuring Datasource");

        String dataSourceJndiName = env.getProperty("datasource.jndi.name");
        if (StringUtils.isNotEmpty(dataSourceJndiName)) {

            LOG.info("Using jndi datasource '" + dataSourceJndiName + "'");
            JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
            dsLookup.setResourceRef(
                env.getProperty(
                    "datasource.jndi.resourceRef", Boolean.class, Boolean.TRUE));
            return dsLookup.getDataSource(dataSourceJndiName);

        } else {

            String dataSourceDriver =
                env.getProperty("datasource.driver", "org.h2.Driver");
            String dataSourceUrl = env.getProperty(
                "datasource.url", "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000");

            String dataSourceUsername = env.getProperty("datasource.username", "sa");
            String dataSourcePassword = env.getProperty("datasource.password", "");

            Integer minPoolSize =
                env.getProperty("datasource.min-pool-size", Integer.class);
            if (minPoolSize == null) {
                minPoolSize = 5;
            }

            Integer maxPoolSize =
                env.getProperty("datasource.max-pool-size", Integer.class);
            if (maxPoolSize == null) {
                maxPoolSize = 50;
            }

            Integer acquireIncrement =
                env.getProperty("datasource.acquire-increment", Integer.class);
            if (acquireIncrement == null) {
                acquireIncrement = 2;
            }

            String preferredTestQuery =
                env.getProperty("datasource.preferred-test-query");

            Boolean testConnectionOnCheckin =
                env.getProperty("datasource.test-connection-on-checkin", Boolean.class);
            if (testConnectionOnCheckin == null) {
                testConnectionOnCheckin = true;
            }

            Boolean testConnectionOnCheckOut = env
                .getProperty("datasource.test-connection-on-checkout", Boolean.class);
            if (testConnectionOnCheckOut == null) {
                testConnectionOnCheckOut = true;
            }

            Integer maxIdleTime =
                env.getProperty("datasource.max-idle-time", Integer.class);
            if (maxIdleTime == null) {
                maxIdleTime = 1800;
            }

            Integer maxIdleTimeExcessConnections = env.getProperty(
                "datasource.max-idle-time-excess-connections", Integer.class);
            if (maxIdleTimeExcessConnections == null) {
                maxIdleTimeExcessConnections = 1800;
            }

            if (LOG.isInfoEnabled()) {
                LOG.info(
                    "Configuring Datasource with following properties (omitted password for security)");
                LOG.info("datasource driver: " + dataSourceDriver);
                LOG.info("datasource url : " + dataSourceUrl);
                LOG.info("datasource user name : " + dataSourceUsername);
                LOG.info(
                    "Min pool size | Max pool size | acquire increment : " + minPoolSize
                        + " | " + maxPoolSize + " | " + acquireIncrement);
            }

            ComboPooledDataSource ds = new ComboPooledDataSource();
            try {
                ds.setDriverClass(dataSourceDriver);
            } catch (PropertyVetoException e) {
                LOG.error("Could not set Jdbc Driver class", e);
                return null;
            }

            // Connection settings
            ds.setJdbcUrl(dataSourceUrl);
            ds.setUser(dataSourceUsername);
            ds.setPassword(dataSourcePassword);

            // Pool config: see http://www.mchange.com/projects/c3p0/#configuration
            ds.setMinPoolSize(minPoolSize);
            ds.setMaxPoolSize(maxPoolSize);
            ds.setAcquireIncrement(acquireIncrement);
            if (preferredTestQuery != null) {
                ds.setPreferredTestQuery(preferredTestQuery);
            }
            ds.setTestConnectionOnCheckin(testConnectionOnCheckin);
            ds.setTestConnectionOnCheckout(testConnectionOnCheckOut);
            ds.setMaxIdleTimeExcessConnections(maxIdleTimeExcessConnections);
            ds.setMaxIdleTime(maxIdleTime);

            return ds;
        }
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        DataSourceTransactionManager transactionManager =
            new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource());
        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(annotationDrivenTransactionManager());
    }

}
