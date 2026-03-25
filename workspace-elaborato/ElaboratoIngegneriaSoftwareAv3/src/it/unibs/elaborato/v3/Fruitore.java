package it.unibs.elaborato.v3;

import java.util.*;

public class Fruitore {
	private String username;
	private List<Notifica> spazioPersonale = new ArrayList<>();

	public Fruitore(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public List<Notifica> getSpazioPersonale() {
		return spazioPersonale;
	}

	public void aggiungiNotifica(Notifica n) {
		spazioPersonale.add(n);
	}

	public void rimuoviNotifica(int index) {
		if (index >= 0 && index < spazioPersonale.size())
			spazioPersonale.remove(index);
	}
}