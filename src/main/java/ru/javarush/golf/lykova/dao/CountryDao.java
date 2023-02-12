package ru.javarush.golf.lykova.dao;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.javarush.golf.lykova.domain.CountryEntity;

import java.util.List;

public class CountryDao {
    private final SessionFactory sessionFactory;

    public CountryDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<CountryEntity> getAll() {
        Query<CountryEntity> query = sessionFactory.getCurrentSession().createQuery("select c from CountryEntity c join fetch c.languages", CountryEntity.class);
        return query.list();
    }
}
