package com.cdq.countries;

public interface CountryLookup {

    String findByName(String name);

    String findByCapital(String capital);
}
