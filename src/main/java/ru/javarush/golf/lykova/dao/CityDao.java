package ru.javarush.golf.lykova.dao;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.javarush.golf.lykova.RelationalDb;
import ru.javarush.golf.lykova.domain.CityEntity;
import ru.javarush.golf.lykova.domain.CountryEntity;
import ru.javarush.golf.lykova.domain.CountryLanguageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CityDao {

    private final RelationalDb relationalDb;

    public CityDao(RelationalDb relationalDb) {
        this.relationalDb = relationalDb;
    }

    public List<CityEntity> find(int offset, int limit) {
        Query<CityEntity> query = relationalDb.getCurrentSession().createQuery("select c from CityEntity c", CityEntity.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }

    public int getTotalCount() {
        Query<Long> query = relationalDb.getCurrentSession().createQuery("select count(c) from CityEntity c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }

    public CityEntity findById(Integer id) {
        Query<CityEntity> query = relationalDb.getCurrentSession().createQuery("select c from CityEntity c join fetch c.countryEntity where c.id = :ID", CityEntity.class);
        query.setParameter("ID", id);
        return query.getSingleResult();
    }

    public List<CityEntity> fetchData(CountryDao countryDao) {
        try (Session session = relationalDb.getCurrentSession()) {
            List<CityEntity> allCities = new ArrayList<>();
            session.beginTransaction();
            List<CountryEntity> countries = countryDao.findAll();

            int totalCount = getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(find(i, step));
            }
            session.getTransaction().commit();
            return allCities;
        }
    }

    public void testMysqlData(List<Integer> ids) {
        try (Session session = relationalDb.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                CityEntity city = findById(id);
                Set<CountryLanguageEntity> languages = city.getCountryEntity().getCountryLanguageEntities();
            }
            session.getTransaction().commit();
        }
    }

}
