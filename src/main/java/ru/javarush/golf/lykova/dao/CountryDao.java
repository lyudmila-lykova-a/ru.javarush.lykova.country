package ru.javarush.golf.lykova.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.javarush.golf.lykova.RelationalDb;
import ru.javarush.golf.lykova.domain.CountryEntity;

import java.util.List;

public class CountryDao {

    private final RelationalDb relationalDb;

    public CountryDao(RelationalDb relationalDb) {
        this.relationalDb = relationalDb;
    }

    public List<CountryEntity> findAll() {
        Query<CountryEntity> query = relationalDb.getCurrentSession().createQuery("select c from CountryEntity c join fetch c.countryLanguageEntities", CountryEntity.class);
        return query.list();
    }

}
