package it.unibs.elaborato.v3;

import java.io.*;
import java.util.*;
import java.time.LocalDate;

public class Configuratore {
	private List<CampoBase> campiBase = new ArrayList<>();
	private List<CampoComune> campiComuni = new ArrayList<>();
	private Map<String, Categoria> categorie = new HashMap<>();
	private Bacheca bachecaReale = new Bacheca();
	private List<Proposta> bozze = new ArrayList<>();
	private Map<String, Fruitore> mappaFruitori = new HashMap<>();

	// --- SETUP E GETTER ---
	public void caricaOInizializzaCampiBase(String path, Scanner sc) throws IOException {
		File f = new File(path);
		if (!f.exists())
			f.createNewFile();
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String l;
			while ((l = br.readLine()) != null)
				if (!l.trim().isEmpty())
					campiBase.add(new CampoBase(l.trim()));
		}
		if (campiBase.isEmpty()) {
			System.out.println(
					"\n[SETUP] Inserisci i campi base. Obbligatori: 'Termine ultimo di iscrizione', 'Data', 'Data conclusiva'");
			try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
				while (true) {
					System.out.print("Nome campo base (o 'FINE'): ");
					String n = sc.nextLine();
					if (n.equalsIgnoreCase("FINE"))
						break;
					if (!n.trim().isEmpty()) {
						campiBase.add(new CampoBase(n.trim()));
						pw.println(n.trim());
					}
				}
			}
		}
	}

	public List<CampoBase> getCampiBase() {
		return campiBase;
	}

	public List<CampoComune> getCampiComuni() {
		return campiComuni;
	}

	public Collection<Categoria> getCategorie() {
		return categorie.values();
	}

	public Fruitore getFruitore(String username) {
		mappaFruitori.putIfAbsent(username, new Fruitore(username));
		return mappaFruitori.get(username);
	}

	// --- METODI CATEGORIE E CAMPI ---
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

	// --- VALIDAZIONE E BOZZE ---
	public boolean validaProposta(Proposta p) {
		for (CampoBase cb : campiBase)
			if (p.getValore(cb.getNome()) == null || p.getValore(cb.getNome()).isBlank())
				return false;
		for (CampoComune cc : campiComuni)
			if (cc.isObbligatorio() && (p.getValore(cc.getNome()) == null || p.getValore(cc.getNome()).isBlank()))
				return false;
		for (CampoSpecifico cs : p.getCategoria().getCampiSpecifici())
			if (cs.isObbligatorio() && (p.getValore(cs.getNome()) == null || p.getValore(cs.getNome()).isBlank()))
				return false;
		LocalDate t = ValidatoreData.parse(p.getValore("Termine ultimo di iscrizione"));
		LocalDate d = ValidatoreData.parse(p.getValore("Data"));
		return ValidatoreData.isTermineValido(t) && ValidatoreData.isDistanzaMinimaRispettata(t, d);
	}

	public void aggiungiBozza(Proposta p) {
		if (validaProposta(p)) {
			p.cambiaStato("Valida");
			bozze.add(p);
		}
	}

	public List<Proposta> getBozze() {
		return bozze;
	}

	public boolean pubblicaBozza(int indice) {
		if (indice >= 0 && indice < bozze.size()) {
			Proposta p = bozze.remove(indice);
			p.cambiaStato("Aperta");
			bachecaReale.aggiungi(p);
			return true;
		}
		return false;
	}

	public void visualizzaBachecaFiltrata(String nomeCat) {
		List<Proposta> filtrate = bachecaReale.filtraPerCategoria(nomeCat);
		if (filtrate.isEmpty())
			System.out.println("Nessuna proposta aperta trovata.");
		else
			filtrate.forEach(System.out::println);
	}

	public void visualizzaBachecaIntera() {
		bachecaReale.getAperte().forEach(System.out::println);
	}

	public Bacheca getBacheca() {
		return bachecaReale;
	}

	// --- V3: MOTORE TEMPORALE E NOTIFICHE ---
	public void aggiornaStatiEInviaNotifiche() {
		LocalDate oggi = LocalDate.now();
		for (Proposta p : bachecaReale.getTutte()) {
			String stato = p.getStatoAttuale();

			if (stato.equals("Aperta")) {
				LocalDate scadenza = ValidatoreData.parse(p.getValore("Termine ultimo di iscrizione"));
				if (scadenza != null && oggi.isAfter(scadenza)) {
					if (p.getIscritti().size() == p.getMaxPartecipanti()) {
						p.cambiaStato("Confermata");
						inviaNotificaA(p.getIscritti(),
								"L'iniziativa '" + p.getValore("Titolo") + "' è CONFERMATA! Ricorda: Data "
										+ p.getValore("Data") + ", Luogo " + p.getValore("Luogo"));
					} else {
						p.cambiaStato("Annullata");
						inviaNotificaA(p.getIscritti(), "L'iniziativa '" + p.getValore("Titolo")
								+ "' è stata ANNULLATA per mancato raggiungimento iscritti.");
					}
				}
			} else if (stato.equals("Confermata")) {
				String dcStr = p.getValore("Data conclusiva");
				LocalDate fine = (dcStr != null && !dcStr.isBlank()) ? ValidatoreData.parse(dcStr)
						: ValidatoreData.parse(p.getValore("Data"));
				if (fine != null && oggi.isAfter(fine)) {
					p.cambiaStato("Conclusa");
				}
			}
		}
	}

	private void inviaNotificaA(List<String> iscritti, String messaggio) {
		Notifica n = new Notifica(messaggio, LocalDate.now());
		for (String username : iscritti) {
			getFruitore(username).aggiungiNotifica(n);
		}
	}

	// --- SALVATAGGIO E CARICAMENTO ---
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
			for (Proposta p : bachecaReale.getTutte())
				pw.println(p.esportaPerArchivio());
			pw.println("FRUITORI_NOTIFICHE");
			for (Fruitore f : mappaFruitori.values()) {
				pw.print(f.getUsername());
				for (Notifica n : f.getSpazioPersonale()) {
					pw.print("##" + ValidatoreData.format(n.getData()) + "||" + n.getMessaggio());
				}
				pw.println();
			}
		}
	}

	private LocalDate parseDataSicura(String dataString) {
		LocalDate dataFormatItaliano = ValidatoreData.parse(dataString);
		if (dataFormatItaliano != null)
			return dataFormatItaliano;
		try {
			return LocalDate.parse(dataString);
		} catch (Exception e) {
			return LocalDate.now();
		}
	}

	public void caricaDati(String path) throws IOException {
		File f = new File(path);
		if (!f.exists())
			return;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String sez = "", l;
			while ((l = br.readLine()) != null) {
				if (l.equals("COMUNI") || l.equals("CATEGORIE") || l.equals("ARCHIVIO_PROPOSTE")
						|| l.equals("FRUITORI_NOTIFICHE")) {
					sez = l;
					continue;
				}

				if (sez.equals("COMUNI")) {
					String[] p = l.split(";");
					campiComuni.add(new CampoComune(p[0], Boolean.parseBoolean(p[1])));
				} else if (sez.equals("CATEGORIE")) {
					String[] p = l.split(";");
					Categoria c = new Categoria(p[0]);
					for (int i = 1; i < p.length; i++) {
						String[] d = p[i].split(",");
						c.aggiungiCampoSpecifico(new CampoSpecifico(d[0], Boolean.parseBoolean(d[1])));
					}
					categorie.put(c.getNome(), c);
				} else if (sez.equals("ARCHIVIO_PROPOSTE")) {
					// Lettura corretta e blindata!
					String[] p = l.split("##");
					Categoria cat = categorie.get(p[0]);
					if (cat != null) {
						Proposta prop = new Proposta(cat);
						if (p.length > 1 && !p[1].isBlank()) {
							for (String val : p[1].split("~")) {
								String[] kv = val.split("=", 2);
								if (kv.length == 2)
									prop.inserisciValore(kv[0], kv[1]);
							}
						}
						if (p.length > 2 && !p[2].isBlank()) {
							for (String st : p[2].split("~")) {
								String[] kv = st.split("=", 2);
								if (kv.length == 2)
									prop.getStoricoStati().add(new RecordStato(kv[0], parseDataSicura(kv[1])));
							}
						}
						if (p.length > 3 && !p[3].isBlank()) {
							for (String is : p[3].split("~")) {
								if (!is.isBlank())
									prop.getIscritti().add(is);
							}
						}
						bachecaReale.aggiungi(prop);
					}
				} else if (sez.equals("FRUITORI_NOTIFICHE")) {
					String[] p = l.split("##");
					Fruitore fr = getFruitore(p[0]);
					for (int i = 1; i < p.length; i++) {
						String[] n = p[i].split("\\|\\|");
						if (n.length == 2)
							fr.aggiungiNotifica(new Notifica(n[1], parseDataSicura(n[0])));
					}
				}
			}
		}
	}
}