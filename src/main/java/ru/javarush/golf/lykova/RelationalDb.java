package ru.javarush.golf.lykova;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import ru.javarush.golf.lykova.domain.CityEntity;
import ru.javarush.golf.lykova.domain.CountryEntity;
import ru.javarush.golf.lykova.domain.CountryLanguageEntity;

import java.util.Properties;

public class RelationalDb implements AutoCloseable {

    private final SessionFactory sessionFactory;

    public RelationalDb() {
        this.sessionFactory = prepareRelationalDb();
    }

    public Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public void close() {
        sessionFactory.close();
    }

    public void closeCurrentSession() {
        sessionFactory.getCurrentSession().close();
    }

    private SessionFactory prepareRelationalDb() {
        final SessionFactory sessionFactory;
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/world");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "my-secret-pw");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        properties.put(Environment.HBM2DDL_AUTO, "validate");
        properties.put(Environment.STATEMENT_BATCH_SIZE, "100");

        sessionFactory = new Configuration()
                .addAnnotatedClass(CityEntity.class)
                .addAnnotatedClass(CountryEntity.class)
                .addAnnotatedClass(CountryLanguageEntity.class)
                .addProperties(properties)
                .buildSessionFactory();
        return sessionFactory;
    }

}
