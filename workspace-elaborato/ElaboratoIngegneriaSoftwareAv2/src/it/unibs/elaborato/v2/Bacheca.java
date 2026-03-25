package it.unibs.elaborato.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Bacheca {
    private List<Proposta> proposteAperte = new ArrayList<>();

    public void aggiungi(Proposta p) {
        proposteAperte.add(p);
    }

    public List<Proposta> getTutte() {
        return proposteAperte;
    }

    public List<Proposta> filtraPerCategoria(String nomeCategoria) {
        return proposteAperte.stream()
                .filter(p -> p.getCategoria().getNome().equalsIgnoreCase(nomeCategoria))
                .collect(Collectors.toList());
    }

    public void svuota() {
        proposteAperte.clear();
    }
}