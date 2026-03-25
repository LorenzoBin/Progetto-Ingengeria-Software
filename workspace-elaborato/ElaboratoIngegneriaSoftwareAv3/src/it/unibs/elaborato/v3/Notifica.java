package it.unibs.elaborato.v3;

import java.time.LocalDate;

public class Notifica {
	private String messaggio;
	private LocalDate data;

	public Notifica(String messaggio, LocalDate data) {
		this.messaggio = messaggio;
		this.data = data;
	}

	public String getMessaggio() {
		return messaggio;
	}

	public LocalDate getData() {
		return data;
	}

	@Override
	public String toString() {
		return "[" + ValidatoreData.format(data) + "] " + messaggio;
	}
}