package ru.javarush.golf.lykova;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import ru.javarush.golf.lykova.dao.CityDao;
import ru.javarush.golf.lykova.dao.CountryDao;
import ru.javarush.golf.lykova.domain.CityEntity;
import ru.javarush.golf.lykova.domain.CountryEntity;
import ru.javarush.golf.lykova.domain.CountryLanguageEntity;
import ru.javarush.golf.lykova.redis.CityCountry;
import ru.javarush.golf.lykova.redis.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class Main {
    private final SessionFactory sessionFactory;
    private final RedisClient redisClient;

    private final ObjectMapper mapper;

    private final CityDao cityDao;
    private final CountryDao countryDao;

    public static void main(String[] args) {
        Main main = new Main();
        List<CityEntity> allCities = main.fetchData();
        List<CityCountry> preparedData = main.transformData(allCities);
        main.pushToRedis(preparedData);
        main.sessionFactory.getCurrentSession().close();

        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        main.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        main.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));

        main.shutdown();
    }

    public Main() {
        sessionFactory = prepareRelationalDb();
        cityDao = new CityDao(sessionFactory);
        countryDao = new CountryDao(sessionFactory);

        redisClient = prepareRedisClient();
        mapper = new ObjectMapper();
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

    private void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

    private List<CityEntity> fetchData() {
        try (Session session = sessionFactory.getCurrentSession()) {
            List<CityEntity> allCities = new ArrayList<>();
            session.beginTransaction();
            List<CountryEntity> countries = countryDao.getAll();

            int totalCount = cityDao.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(cityDao.getItems(i, step));
            }
            session.getTransaction().commit();
            return allCities;
        }
    }

    private RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }
        return redisClient;
    }

    private List<CityCountry> transformData(List<CityEntity> cities) {
        return cities.stream().map(cityEntity -> {
            CityCountry res = new CityCountry();
            res.setId(cityEntity.getId());
            res.setName(cityEntity.getName());
            res.setPopulation(cityEntity.getPopulation());
            res.setDistrict(cityEntity.getDistrict());

            CountryEntity countryEntity = cityEntity.getCountryEntity();
            res.setAlternativeCountryCode(countryEntity.getAlternativeCode());
            res.setContinent(countryEntity.getContinent());
            res.setCountryCode(countryEntity.getCode());
            res.setCountryName(countryEntity.getName());
            res.setCountryPopulation(countryEntity.getPopulation());
            res.setCountryRegion(countryEntity.getRegion());
            res.setCountrySurfaceArea(countryEntity.getSurfaceArea());
            Set<CountryLanguageEntity> countryLanguageEntities = countryEntity.getLanguages();
            Set<Language> languages = countryLanguageEntities.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setOfficial(cl.getOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguages(languages);

            return res;
        }).collect(Collectors.toList());
    }

    private void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), mapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    mapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                CityEntity city = cityDao.getById(id);
                Set<CountryLanguageEntity> languages = city.getCountryEntity().getLanguages();
            }
            session.getTransaction().commit();
        }
    }
}