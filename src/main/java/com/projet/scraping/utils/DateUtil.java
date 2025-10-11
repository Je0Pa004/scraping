package com.projet.scraping.utils;

import com.projet.scraping.entities.enums.SubscriptionType;

import java.time.LocalDate;

public final class DateUtil {
    private DateUtil() {}

    public static LocalDate computeEndDate(LocalDate start, SubscriptionType type) {
        return switch (type) {
            case MENSUEL -> start.plusMonths(1);
            case TRIMESTRIEL -> start.plusMonths(3);
            case ANNUEL -> start.plusYears(1);
        };
    }
}
