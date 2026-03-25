package it.unibs.elaborato.v3;

import java.util.*;
import java.util.stream.Collectors;

public class Bacheca {
	private List<Proposta> proposte = new ArrayList<>();

	public void aggiungi(Proposta p) {
		proposte.add(p);
	}

	public List<Proposta> getTutte() {
		return proposte;
	}

	// In V3 la bacheca mostra SOLO quelle Aperte
	public List<Proposta> getAperte() {
		return proposte.stream().filter(p -> p.getStatoAttuale().equals("Aperta")).collect(Collectors.toList());
	}

	public List<Proposta> filtraPerCategoria(String nomeCategoria) {
		return getAperte().stream().filter(p -> p.getCategoria().getNome().equalsIgnoreCase(nomeCategoria))
				.collect(Collectors.toList());
	}
}