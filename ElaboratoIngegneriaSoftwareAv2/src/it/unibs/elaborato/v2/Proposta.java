package it.unibs.elaborato.v2;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Proposta {
    private final Categoria categoria;
    private final Map<String, String> valoriInseriti = new HashMap<>();
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

    @Override
    public String toString() {
        return String.format("\n[PROPOSTA %s] Categoria: %s\nPubblicata il: %s\nDati: %s", 
                stato.toUpperCase(), categoria.getNome(), ValidatoreData.format(dataPubblicazione), valoriInseriti);
    }
}