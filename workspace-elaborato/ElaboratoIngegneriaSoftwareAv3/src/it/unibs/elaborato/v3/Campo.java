package it.unibs.elaborato.v3;

import java.io.Serializable;

public abstract class Campo implements Serializable {
	private final String nome;
	private boolean obbligatorio;

	public Campo(String nome, boolean obbligatorio) {
		this.nome = nome;
		this.obbligatorio = obbligatorio;
	}

	public String getNome() {
		return nome;
	}

	public boolean isObbligatorio() {
		return obbligatorio;
	}

	public void setObbligatorio(boolean obbligatorio) {
		this.obbligatorio = obbligatorio;
	}

	@Override
	public String toString() {
		return nome + (obbligatorio ? " [Obbligatorio]" : " [Facoltativo]");
	}
}