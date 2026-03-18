package it.unibs.elaborato.v2;

import java.io.*;
import java.util.*;
import java.time.LocalDate;

/**
 * Logica di business per la gestione di campi, categorie e proposte (V2).
 */
public class Configuratore {
	private List<CampoBase> campiBase = new ArrayList<>();
	private List<CampoComune> campiComuni = new ArrayList<>();
	private Map<String, Categoria> categorie = new HashMap<>();
	private Bacheca bachecaReale = new Bacheca();

	// Inizializza i campi base da file o da tastiera se il file è vuoto
	public void caricaOInizializzaCampiBase(String path, Scanner sc) throws IOException {
		File f = new File(path);
		if (!f.exists())
			f.createNewFile();

		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String l;
			while ((l = br.readLine()) != null) {
				if (!l.trim().isEmpty())
					campiBase.add(new CampoBase(l.trim()));
			}
		}

		if (campiBase.isEmpty()) {
			System.out.println("\n[SETUP] Il file campi_base.txt è vuoto. Inserimento obbligatorio:");
			try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
				while (true) {
					System.out.print("Nome campo base (o 'FINE'): ");
					String nome = sc.nextLine();
					if (nome.equalsIgnoreCase("FINE"))
						break;
					if (!nome.trim().isEmpty()) {
						campiBase.add(new CampoBase(nome.trim()));
						pw.println(nome.trim());
					}
				}
			}
		}
	}

	public void aggiungiCampoComune(String n, boolean o) {
		campiComuni.add(new CampoComune(n, o));
	}

	public void rimuoviCampoComune(String n) {
		campiComuni.removeIf(c -> c.getNome().equalsIgnoreCase(n));
	}

	public void cambiaObbligatorietaComune(String n, boolean o) {
		for (CampoComune c : campiComuni)
			if (c.getNome().equalsIgnoreCase(n))
				c.setObbligatorio(o);
	}

	public void creaCategoria(String n) {
		categorie.putIfAbsent(n, new Categoria(n));
	}

	public void rimuoviCategoria(String n) {
		categorie.remove(n);
	}

	public void aggiungiCampoSpecifico(String cat, String n, boolean o) {
		if (categorie.containsKey(cat))
			categorie.get(cat).aggiungiCampoSpecifico(new CampoSpecifico(n, o));
	}

	public void rimuoviCampoSpecifico(String cat, String n) {
		if (categorie.containsKey(cat))
			categorie.get(cat).getCampiSpecifici().removeIf(c -> c.getNome().equalsIgnoreCase(n));
	}

	// --- METODI AGGIUNTI PER LA VERSIONE 2 ---

	public boolean validaProposta(Proposta p) {
		// 1. Controllo campi obbligatori (Base, Comuni, Specifici)
		for (CampoBase cb : campiBase) 
			if (p.getValore(cb.getNome()) == null || p.getValore(cb.getNome()).isBlank()) return false;
		
		for (CampoComune cc : campiComuni) 
			if (cc.isObbligatorio() && (p.getValore(cc.getNome()) == null || p.getValore(cc.getNome()).isBlank())) return false;
		
		for (CampoSpecifico cs : p.getCategoria().getCampiSpecifici()) 
			if (cs.isObbligatorio() && (p.getValore(cs.getNome()) == null || p.getValore(cs.getNome()).isBlank())) return false;

		// 2. Controllo vincoli temporali
		LocalDate t = ValidatoreData.parse(p.getValore("Termine ultimo di iscrizione")); //NB ricordarsi di mettere cosi questi 2 campi base
		LocalDate d = ValidatoreData.parse(p.getValore("Data"));

		return ValidatoreData.isTermineValido(t) && ValidatoreData.isDistanzaMinimaRispettata(t, d);
	}
	public void pubblicaInBacheca(Proposta p) {
		if (validaProposta(p)) {
			p.setStato("Aperta");
			p.setDataPubblicazione(LocalDate.now());
			bachecaReale.aggiungi(p);
		}
	}

	public void visualizzaBachecaFiltrata(String nomeCat) {
		List<Proposta> filtrate = bachecaReale.filtraPerCategoria(nomeCat);
		System.out.println("\n--- BACHECA CATEGORIA: " + nomeCat.toUpperCase() + " ---");
		if (filtrate.isEmpty()) {
			System.out.println("Nessuna proposta aperta trovata.");
		} else {
			filtrate.forEach(System.out::println);
		}
	}

	// --- SALVATAGGIO E CARICAMENTO (Aggiornati per V2) ---

	public void salvaDati(String path) throws IOException {
		try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
			pw.println("COMUNI");
			for (CampoComune cc : campiComuni)
				pw.println(cc.getNome() + ";" + cc.isObbligatorio());
			
			pw.println("CATEGORIE");
			for (Categoria cat : categorie.values()) {
				pw.print(cat.getNome());
				for (CampoSpecifico cs : cat.getCampiSpecifici())
					pw.print(";" + cs.getNome() + "," + cs.isObbligatorio());
				pw.println();
			}

			pw.println("ARCHIVIO_PROPOSTE");
			for (Proposta p : bachecaReale.getTutte()) {
				pw.println(p.esportaPerArchivio());
			}
		}
	}

	public void caricaDati(String path) throws IOException {
		File f = new File(path);
		if (!f.exists())
			return;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String sezione = "", l;
			while ((l = br.readLine()) != null) {
				if (l.equals("COMUNI") || l.equals("CATEGORIE") || l.equals("ARCHIVIO_PROPOSTE")) {
					sezione = l;
					continue;
				}
				if (sezione.equals("COMUNI")) {
					String[] p = l.split(";");
					campiComuni.add(new CampoComune(p[0], Boolean.parseBoolean(p[1])));
				} else if (sezione.equals("CATEGORIE")) {
					String[] p = l.split(";");
					Categoria c = new Categoria(p[0]);
					for (int i = 1; i < p.length; i++) {
						String[] d = p[i].split(",");
						c.aggiungiCampoSpecifico(new CampoSpecifico(d[0], Boolean.parseBoolean(d[1])));
					}
					categorie.put(c.getNome(), c);
				} else if (sezione.equals("ARCHIVIO_PROPOSTE")) {
					String[] p = l.split("\\|");
					Categoria cat = categorie.get(p[0]);
					if (cat != null) {
						Proposta prop = new Proposta(cat);
						prop.setDataPubblicazione(LocalDate.parse(p[1]));
						prop.setStato(p[2]);
						for (int i = 3; i < p.length; i++) {
							String[] kv = p[i].split(":", 2); // Split in 2 nel caso ci siano ':' nel valore
							if (kv.length == 2) prop.inserisciValore(kv[0], kv[1]);
						}
						bachecaReale.aggiungi(prop);
					}
				}
			}
		}
	}

	// Getter necessari per l'interfaccia
	public List<CampoBase> getCampiBase() {
		return campiBase;
	}

	public List<CampoComune> getCampiComuni() {
		return campiComuni;
	}

	public Collection<Categoria> getCategorie() {
		return categorie.values();
	}
}