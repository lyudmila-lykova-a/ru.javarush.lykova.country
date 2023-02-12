package ru.javarush.golf.lykova.dao;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import ru.javarush.golf.lykova.domain.CityEntity;

import java.util.List;

public class CityDao {
    private final SessionFactory sessionFactory;

    public CityDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<CityEntity> getItems(int offset, int limit) {
        Query<CityEntity> query = sessionFactory.getCurrentSession().createQuery("select c from CityEntity c", CityEntity.class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.list();
    }

    public int getTotalCount() {
        Query<Long> query = sessionFactory.getCurrentSession().createQuery("select count(c) from CityEntity c", Long.class);
        return Math.toIntExact(query.uniqueResult());
    }

    public CityEntity getById(Integer id) {
        Query<CityEntity> query = sessionFactory.getCurrentSession().createQuery("select c from CityEntity c join fetch c.countryEntity where c.id = :ID", CityEntity.class);
        query.setParameter("ID", id);
        return query.getSingleResult();
    }
}
