package com.tauk.carsapp;

public class City {
    public String name;
    public double area;
    public double population;

    public City(String name, double area, double population) {
        this.name = name;
        this.area = area;
        this.population = population;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", area=" + area +
                ", population=" + population +
                '}';
    }

    public City() {

    }
}
