package it.unibs.elaborato.v3;

import java.time.LocalDate;

public class RecordStato {
	private String stato;
	private LocalDate data;

	public RecordStato(String stato, LocalDate data) {
		this.stato = stato;
		this.data = data;
	}

	public String getStato() {
		return stato;
	}

	public LocalDate getData() {
		return data;
	}

	@Override
	public String toString() {
		return stato + " (" + ValidatoreData.format(data) + ")";
	}
}