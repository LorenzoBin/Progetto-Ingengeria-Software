package it.unibs.elaborato.v3;

public class CampoBase extends Campo {
	public CampoBase(String nome) {
		super(nome, true);
	}

	@Override
	public void setObbligatorio(boolean obbligatorio) {
		throw new UnsupportedOperationException("I campi base sono immutabili e sempre obbligatori.");
	}
}