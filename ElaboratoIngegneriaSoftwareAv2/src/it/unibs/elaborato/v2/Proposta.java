package it.unibs.elaborato.v2;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Proposta {
	private final Categoria categoria;
	// Usando LinkedHashMap garantiamo che l'ordine di inserimento venga rispettato
	private final Map<String, String> valoriInseriti = new LinkedHashMap<>();
	private String stato;
	private LocalDate dataPubblicazione;

	public Proposta(Categoria categoria) {
		this.categoria = categoria;
		this.stato = "In compilazione";
	}

	public void inserisciValore(String nomeCampo, String valore) {
		valoriInseriti.put(nomeCampo, valore);
	}

	public String getValore(String nomeCampo) {
		return valoriInseriti.get(nomeCampo);
	}

	public String getStato() { return stato; }
	public void setStato(String stato) { this.stato = stato; }
	public void setDataPubblicazione(LocalDate data) { this.dataPubblicazione = data; }
	public Categoria getCategoria() { return categoria; }

	public String esportaPerArchivio() {
		StringBuilder sb = new StringBuilder();
		sb.append(categoria.getNome()).append("|")
		  .append(dataPubblicazione).append("|")
		  .append(stato);
		for (Map.Entry<String, String> entry : valoriInseriti.entrySet()) {
			sb.append("|").append(entry.getKey()).append(":").append(entry.getValue());
		}
		return sb.toString();
	}

	/**
	 * Nuovo metodo per stampare la proposta in modo pulito e ordinato:
	 * 1. Campi Base 2. Campi Comuni 3. Campi Specifici
	 */
	public String stampaFormattata(List<CampoBase> campiBase, List<CampoComune> campiComuni) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\n========================================\n[PROPOSTA %s] Categoria: %s\nPubblicata il: %s\n",
				stato.toUpperCase(), categoria.getNome(), ValidatoreData.format(dataPubblicazione)));

		sb.append("\n--- Campi Base ---\n");
		for (CampoBase cb : campiBase) {
			String val = valoriInseriti.get(cb.getNome());
			if (val != null) sb.append("- ").append(cb.getNome()).append(": ").append(val).append("\n");
		}

		if (!campiComuni.isEmpty()) {
			sb.append("\n--- Campi Comuni ---\n");
			for (CampoComune cc : campiComuni) {
				String val = valoriInseriti.get(cc.getNome());
				if (val != null) sb.append("- ").append(cc.getNome()).append(": ").append(val).append("\n");
			}
		}

		if (!categoria.getCampiSpecifici().isEmpty()) {
			sb.append("\n--- Campi Specifici ---\n");
			for (CampoSpecifico cs : categoria.getCampiSpecifici()) {
				String val = valoriInseriti.get(cs.getNome());
				if (val != null) sb.append("- ").append(cs.getNome()).append(": ").append(val).append("\n");
			}
		}
		sb.append("========================================");
		return sb.toString();
	}

	// Lasciamo il toString classico per sicurezza/debug
	@Override
	public String toString() {
		return String.format("\n[PROPOSTA %s] Categoria: %s | Dati: %s", stato.toUpperCase(), categoria.getNome(), valoriInseriti);
	}
}