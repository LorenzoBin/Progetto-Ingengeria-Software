package it.unibs.elaborato.v2;

public class CampoBase extends Campo {
	public CampoBase(String nome) {
		super(nome, true);
	}

	@Override
	public void setObbligatorio(boolean obbligatorio) {
		// Invariante: i campi base restano sempre obbligatori
		throw new UnsupportedOperationException("I campi base sono immutabili.");
	}
}