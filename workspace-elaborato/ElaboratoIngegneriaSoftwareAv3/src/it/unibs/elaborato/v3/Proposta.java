package it.unibs.elaborato.v3;

import java.time.LocalDate;
import java.util.*;

public class Proposta {
	private final Categoria categoria;
	private final Map<String, String> valoriInseriti = new HashMap<>();
	private List<RecordStato> storicoStati = new ArrayList<>();
	private List<String> iscritti = new ArrayList<>(); // Lista username fruitori

	public Proposta(Categoria categoria) {
		this.categoria = categoria;
	}

	public void inserisciValore(String nomeCampo, String valore) {
		valoriInseriti.put(nomeCampo, valore);
	}

	public String getValore(String nomeCampo) {
		return valoriInseriti.get(nomeCampo);
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public List<String> getIscritti() {
		return iscritti;
	}

	public int getMaxPartecipanti() {
		try {
			return Integer.parseInt(valoriInseriti.getOrDefault("Numero di partecipanti", "0"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getStatoAttuale() {
		if (storicoStati.isEmpty())
			return "In compilazione";
		return storicoStati.get(storicoStati.size() - 1).getStato();
	}

	public void cambiaStato(String nuovoStato) {
		storicoStati.add(new RecordStato(nuovoStato, LocalDate.now()));
	}

	public List<RecordStato> getStoricoStati() {
		return storicoStati;
	}

	public boolean puoIscriversi(String username) {
		if (!getStatoAttuale().equals("Aperta"))
			return false;
		if (iscritti.contains(username))
			return false;
		if (iscritti.size() >= getMaxPartecipanti())
			return false;
		return true;
	}

	public boolean iscrivi(String username) {
		if (puoIscriversi(username)) {
			iscritti.add(username);
			return true;
		}
		return false;
	}

	// Export corretto e blindato! Separatore primario: ##
	public String esportaPerArchivio() {
		StringBuilder sb = new StringBuilder(categoria.getNome() + "##");
		for (Map.Entry<String, String> e : valoriInseriti.entrySet()) {
			sb.append(e.getKey()).append("=").append(e.getValue()).append("~");
		}
		sb.append("##");
		for (RecordStato rs : storicoStati) {
			sb.append(rs.getStato()).append("=").append(ValidatoreData.format(rs.getData())).append("~");
		}
		sb.append("##");
		if (!iscritti.isEmpty()) {
			sb.append(String.join("~", iscritti));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		String base = String.format("\n[%s] Categoria: %s | Titolo: %s", getStatoAttuale().toUpperCase(),
				categoria.getNome(), getValore("Titolo"));
		if (getStatoAttuale().equals("Aperta") || getStatoAttuale().equals("Confermata")) {
			base += String.format("\n    Iscritti: %d/%d | Scadenza: %s | Evento: %s", iscritti.size(),
					getMaxPartecipanti(), getValore("Termine ultimo di iscrizione"), getValore("Data"));
		}
		return base;
	}
}