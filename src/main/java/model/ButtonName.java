package model;

import lombok.Getter;

@Getter
public enum ButtonName {
    BACK("Спина"),
    CHEST("Грудь"),
    LEGS("Ноги"),
    BACKBUTTON ("Назад"),
    WEIGHTGAIN ("Набор массы");

    private final String name;

    ButtonName(String name) {
        this.name = name;
    }

}
