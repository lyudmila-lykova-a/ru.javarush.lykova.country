package ru.javarush.golf.lykova;

import ru.javarush.golf.lykova.dao.CityDao;
import ru.javarush.golf.lykova.dao.CountryDao;

public class Main {

    public static void main(String[] args) {
        try (RelationalDb relationalDb = new RelationalDb();
             Redis redis = new Redis()) {

            CityDao cityDao = new CityDao(relationalDb);
            CountryDao countryDao = new CountryDao(relationalDb);

            CompareRelationalDbWithRedis compareRelationalDbWithRedis =
                    new CompareRelationalDbWithRedis(relationalDb, redis, cityDao, countryDao);
            compareRelationalDbWithRedis.compare();
        }
    }

}