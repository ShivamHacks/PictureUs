package com.example.shivamagrawal.photoshareapp.Objects;

public class Contact {
    String name;
    String number;
    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }
    @Override
    public String toString() {
        return number;
    }
    public String getName() { return name; }
    public String getNumber() { return number; }
}