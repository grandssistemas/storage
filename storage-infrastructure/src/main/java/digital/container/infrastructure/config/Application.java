package digital.container.infrastructure.config;


import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import digital.container.storage.domain.model.util.AmazonS3Util;
import io.gumga.core.GumgaValues;

import io.gumga.application.GumgaRepositoryFactoryBean;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import io.gumga.domain.CriterionParser;
import io.gumga.domain.GumgaQueryParserProvider;

import javax.annotation.PostConstruct;
import javax.jms.Session;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.jboss.logging.annotations.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@ComponentScan(basePackages = {"digital.container", "io.gumga"})
@EnableJpaRepositories(repositoryFactoryBeanClass = GumgaRepositoryFactoryBean.class, basePackages = {"digital.container", "io.gumga"})
@EnableTransactionManagement(proxyTargetClass = true)
@EnableAsync
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private Properties properties;

    @Autowired
    private GumgaValues gumgaValues;

    private Properties getProperties() {
        if(this.gumgaValues == null)
            this.gumgaValues = new ApplicationConstants();

        if(this.properties == null)
            this.properties = this.gumgaValues.getCustomFileProperties();

        return this.properties;
    }

    @Bean
    public static PropertyPlaceholderConfigurer dataConfigPropertyConfigurer() {
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setSearchSystemEnvironment(true);
        return configurer;
    }

    private Map<Class<?>, CriterionParser> gumgaQueryParseProviderFactory(String name) {
        switch (Database.valueOf(name)) {
            case POSTGRES:
                return GumgaQueryParserProvider.getPostgreSqlLikeMap();
            case MYSQL:
                return GumgaQueryParserProvider.getMySqlLikeMap();
            case ORACLE:
                return GumgaQueryParserProvider.getOracleLikeMap();
            case H2:
                return GumgaQueryParserProvider.getH2LikeMap();
            default: return GumgaQueryParserProvider.getH2LikeMap();
        }
    }

    private HikariConfig commonConfig() {
        GumgaQueryParserProvider.defaultMap = gumgaQueryParseProviderFactory(getProperties().getProperty("name", "H2"));
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(150);
        config.setMinimumIdle(50);
        config.setIdleTimeout(100000L);
        config.setInitializationFailFast(true);
        config.setDataSourceClassName(getProperties().getProperty("dataSource.className", "org.h2.jdbcx.JdbcDataSource"));
        config.addDataSourceProperty("url", getProperties().getProperty("dataSource.url", "jdbc:h2:mem:studio;MVCC=true"));
        config.addDataSourceProperty("user", getProperties().getProperty("dataSource.user", "sa"));
        config.addDataSourceProperty("password", getProperties().getProperty("dataSource.password", "sa"));
        return config;
    }


    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(commonConfig());
    }

    private Properties commonProperties() {
        Properties properties = new Properties();
        properties.setProperty("eclipselink.weaving", "false");
        properties.setProperty("hibernate.ejb.naming_strategy",getProperties().getProperty("hibernate.ejb.naming_strategy", "org.hibernate.cfg.EJB3NamingStrategy"));
        properties.setProperty("hibernate.show_sql", getProperties().getProperty("hibernate.show_sql", "false"));
        properties.setProperty("hibernate.format_sql", getProperties().getProperty("hibernate.format_sql", "false"));
        properties.setProperty("hibernate.connection.charSet", getProperties().getProperty("hibernate.connection.charSet", "UTF-8"));
        properties.setProperty("hibernate.connection.characterEncoding", getProperties().getProperty("hibernate.connection.characterEncoding", "UTF-8"));
        properties.setProperty("hibernate.connection.useUnicode", getProperties().getProperty("hibernate.connection.useUnicode", "true"));
        properties.setProperty("hibernate.jdbc.batch_size", getProperties().getProperty("hibernate.jdbc.batch_size", "500"));
        properties.setProperty("hibernate.hbm2ddl.auto", getProperties().getProperty("hibernate.hbm2ddl.auto", "create-drop"));
        properties.setProperty("hibernate.dialect", getProperties().getProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect"));
        return properties;
    }

    @Bean
    @Autowired
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("io.gumga.domain","digital.container");
        factory.setDataSource(dataSource);
        factory.setJpaProperties(commonProperties());
        factory.afterPropertiesSet();
        return factory;
    }

    @PostConstruct
    public void setPropertiesSystem() {
        System.setProperty("url.mom", getProperties().getProperty("url.mom", "tcp://localhost:61616"));
        System.setProperty("mom.queue", getProperties().getProperty("mom.queue", "tax.document.queue"));

        System.setProperty("storage.localfile", getProperties().getProperty("storage.localfile", "/root/storage-files/perm"));
        System.setProperty("storage.foldertemp", getProperties().getProperty("storage.foldertemp", "/root/storage-files/temp"));

        System.setProperty("url.host", getProperties().getProperty("url.host"));

        System.setProperty("public.token.integration", getProperties().getProperty("public.token.integration", "3cb73f59eb02-479b-b859-797e29eb8256-90703973edf5aa2d"));

        System.setProperty("amazon.s3.access_key_id", getProperties().getProperty("amazon.s3.access_key_id"));
        System.setProperty("amazon.s3.secret_access_key", getProperties().getProperty("amazon.s3.secret_access_key"));
        System.setProperty("amazon.s3.anything_bucket", getProperties().getProperty("amazon.s3.anything_bucket"));
        System.setProperty("amazon.s3.tax_document_bucket", getProperties().getProperty("amazon.s3.tax_document_bucket"));

        System.setProperty("aws.accessKeyId", getProperties().getProperty("amazon.s3.access_key_id"));
        System.setProperty("aws.secretKey", getProperties().getProperty("amazon.s3.secret_access_key"));
    }

    @Bean
    @Autowired
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

//    @Bean
//    public ActiveMQConnectionFactory connectionFactory() {
//        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
//        connectionFactory.setBrokerURL(System.getProperty("url.mom"));
////        SQSConnectionFactory connectionFactory = SQSConnectionFactory
////                .builder()
////                .withRegion(Region.getRegion(Regions.US_WEST_2))
////                .withAWSCredentialsProvider(new SystemPropertiesCredentialsProvider())
////                .build();
//
//        return connectionFactory;
//    }

//    @Bean
//    public JmsTemplate jmsTemplate() {
//        JmsTemplate template = new JmsTemplate();
//        template.setConnectionFactory(connectionFactory());
//        template.setDefaultDestinationName(System.getProperty("mom.queue"));
//
//        return template;
//    }

    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .withCredentials(new SystemPropertiesCredentialsProvider())
                .build();
    }

    @Bean
    public BasicAWSCredentials basicAWSCredentials() {
        return new BasicAWSCredentials(AmazonS3Util.ACCESS_KEY_ID, AmazonS3Util.SECRET_ACCESS_KEY);
    }

    @Bean
    public AmazonS3 amazonS3(BasicAWSCredentials basicAWSCredentials) {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.SA_EAST_1)
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(15);
        pool.setMaxPoolSize(20);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

}

enum Database {
    POSTGRES, MYSQL, ORACLE, H2;
}
