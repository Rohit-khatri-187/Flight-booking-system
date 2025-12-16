package com.rohit.enums;

import lombok.Getter;

@Getter
public enum City {

    LAGOS(Country.NIGERIA),
    ABUJA(Country.NIGERIA),

    MIAMI(Country.USA),
    DALLAS(Country.USA),

    LONDON(Country.UK),
    LEEDS(Country.UK);

    private  final Country country;
    City(Country country){
        this.country = country;
    }

}
