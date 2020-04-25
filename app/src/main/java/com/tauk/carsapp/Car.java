package com.tauk.carsapp;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Car {
    public String carRegNo;
    public String carModelName;
    public int year;
    public double price;
    public String postedByUser;
    public String posterEmail;
    public String posterMobile;

    public Car() {
    }

    public Car(String carRegNo, String carModelName, int year, double price,
               String postedByUser, String posterEmail, String posterMobile) {
        this.carRegNo  = carRegNo;
        this.carModelName = carModelName;
        this.year = year;
        this.price = price;
        this.postedByUser = postedByUser;
        this.posterEmail = posterEmail;
        this.posterMobile = posterMobile;
    }

    @Override
    public String toString() {
        return "\nCarRegNo='" + carRegNo + '\'' +
                ",ModelName='" + carModelName + '\'' +
                ",Year=" + year +
                ",Price=" + price +
                ",postedBy='" + postedByUser + '\'' +
                ",Email='" + posterEmail + '\'' +
                ",Mobile='" + posterMobile + "\n";
    }
}
