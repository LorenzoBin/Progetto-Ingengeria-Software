package it.unibs.elaborato.v2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;

public class ValidatoreData {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    public static LocalDate parse(String dataStr) {
        try {
            if (dataStr == null || dataStr.trim().isEmpty()) return null;
            return LocalDate.parse(dataStr.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isTermineValido(LocalDate termine) {
        LocalDate oggi = LocalDate.now();
        return termine != null && termine.isAfter(oggi);
    }

    public static boolean isDistanzaMinimaRispettata(LocalDate termine, LocalDate inizioEvento) {
        if (termine == null || inizioEvento == null) return false;
        long giorni = ChronoUnit.DAYS.between(termine, inizioEvento);
        return giorni >= 2; // Almeno due giorni di differenza
    }

    public static String format(LocalDate data) {
        return (data == null) ? "N/A" : data.format(FORMATTER);
    }
}