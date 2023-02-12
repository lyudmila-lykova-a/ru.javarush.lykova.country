package ru.javarush.golf.lykova;

import ru.javarush.golf.lykova.dao.CityDao;
import ru.javarush.golf.lykova.dao.CountryDao;
import ru.javarush.golf.lykova.domain.CityEntity;
import ru.javarush.golf.lykova.redis.CityCountry;

import java.util.List;

public class CompareRelationalDbWithRedis {

    private final RelationalDb relationalDb;
    private final Redis redis;
    private final CityDao cityDao;
    private final CountryDao countryDao;

    public CompareRelationalDbWithRedis(RelationalDb relationalDb, Redis redis, CityDao cityDao, CountryDao countryDao) {
        this.relationalDb = relationalDb;
        this.redis = redis;
        this.cityDao = cityDao;
        this.countryDao = countryDao;
    }

    public void compare() {
        List<CityEntity> allCities = cityDao.fetchData(countryDao);
        List<CityCountry> preparedData = Converter.convertCityEntitiesToCityCountryList(allCities);
        redis.pushToRedis(preparedData);
        relationalDb.closeCurrentSession();

        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);

        long startRedis = System.currentTimeMillis();
        redis.testRedisData(ids);
        long stopRedis = System.currentTimeMillis();

        long startMysql = System.currentTimeMillis();
        cityDao.testMysqlData(ids);
        long stopMysql = System.currentTimeMillis();

        System.out.printf("%s:\t%d ms\n", "Redis", (stopRedis - startRedis));
        System.out.printf("%s:\t%d ms\n", "MySQL", (stopMysql - startMysql));
    }

}