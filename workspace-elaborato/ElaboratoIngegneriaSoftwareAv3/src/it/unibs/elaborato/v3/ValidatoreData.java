package it.unibs.elaborato.v3;

import java.time.LocalDate;
import java.time.format.*;
import java.time.temporal.ChronoUnit;

public class ValidatoreData {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu")
			.withResolverStyle(ResolverStyle.STRICT);

	public static LocalDate parse(String dataStr) {
		try {
			if (dataStr == null || dataStr.trim().isEmpty())
				return null;
			return LocalDate.parse(dataStr.trim(), FORMATTER);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	public static boolean isTermineValido(LocalDate termine) {
		return termine != null && termine.isAfter(LocalDate.now());
	}

	public static boolean isDistanzaMinimaRispettata(LocalDate termine, LocalDate inizioEvento) {
		if (termine == null || inizioEvento == null)
			return false;
		return ChronoUnit.DAYS.between(termine, inizioEvento) >= 2;
	}

	public static String format(LocalDate data) {
		return (data == null) ? "N/A" : data.format(FORMATTER);
	}
}