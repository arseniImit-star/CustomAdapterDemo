package com.example.customadapterdemo;

public class User {
    public String name;
    public String phoneNumber;
    public Sex sex;
    // URI выбранной пользователем фотографии. Если пусто — используется стандартная аватарка.
    public String avatarUri;

    public User(String name, String phoneNumber, Sex sex) {
        this(name, phoneNumber, sex, null);
    }

    public User(String name, String phoneNumber, Sex sex, String avatarUri) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.sex = sex;
        this.avatarUri = avatarUri;
    }
}
