package it.unibs.elaborato.v2;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce una categoria di iniziative e i suoi campi specifici.
 */
public class Categoria {
	private final String nome;
	private final List<CampoSpecifico> campiSpecifici = new ArrayList<>();

	public Categoria(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public List<CampoSpecifico> getCampiSpecifici() {
		return campiSpecifici;
	}

	public void aggiungiCampoSpecifico(CampoSpecifico cs) {
		this.campiSpecifici.add(cs);
	}

	public void visualizza(List<CampoBase> base, List<CampoComune> comuni) {
		System.out.println("\n--- CATEGORIA: " + nome.toUpperCase() + " ---");
		System.out.println("Campi Base: " + base);
		System.out.println("Campi Comuni: " + comuni);
		System.out.println("Campi Specifici: " + campiSpecifici);
	}
}