package ru.javarush.golf.lykova;

import ru.javarush.golf.lykova.domain.CityEntity;
import ru.javarush.golf.lykova.domain.CountryEntity;
import ru.javarush.golf.lykova.domain.CountryLanguageEntity;
import ru.javarush.golf.lykova.redis.CityCountry;
import ru.javarush.golf.lykova.redis.Language;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Converter {

    public static List<CityCountry> convertCityEntitiesToCityCountryList(List<CityEntity> cities) {
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
            Set<CountryLanguageEntity> countryLanguageEntities = countryEntity.getCountryLanguageEntities();
            Set<Language> languages = countryLanguageEntities.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setIsOfficial(cl.getIsOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguages(languages);

            return res;
        }).collect(Collectors.toList());
    }

}
