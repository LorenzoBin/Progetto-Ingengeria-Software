package it.unibs.elaborato.v1;

public class CampoBase extends Campo {
	public CampoBase(String nome) {
		super(nome, true);
	}

	@Override
	public void setObbligatorio(boolean obbligatorio) {
		// Invariante: i campi base restano sempre obbligatori [cite: 31]
		throw new UnsupportedOperationException("I campi base sono immutabili.");
	}
}