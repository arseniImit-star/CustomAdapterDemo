package com.example.customadapterdemo;

public enum Sex {
    MAN("Мужчина", R.drawable.avatar_man),
    WOMAN("Женщина", R.drawable.avatar_woman),
    STUDENT("Студент", R.drawable.avatar_student),
    DEVELOPER("Разработчик", R.drawable.avatar_developer),
    UNKNOWN("Другое", R.drawable.avatar_unknown);

    public final String title;
    public final int imageRes;

    Sex(String title, int imageRes) {
        this.title = title;
        this.imageRes = imageRes;
    }

    @Override public String toString() { return title; }
}
