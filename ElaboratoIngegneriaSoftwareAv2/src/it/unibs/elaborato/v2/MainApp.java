package it.unibs.elaborato.v2;

import java.util.*;

import it.unibs.fp.mylib.InputDati;

import java.io.*;

public class MainApp {
	private static final String FILE_CREDENZIALI = "credenziali.txt";
	private static final String FILE_CAMPI_BASE = "campi_base.txt";
	private static final String FILE_DATI_SISTEMA = "dati_sistema.txt";

	public static void main(String[] args) {
		Configuratore config = new Configuratore();
		SessioneLogin login = new SessioneLogin();
		Scanner sc = new Scanner(System.in);

		try {
			login.carica(FILE_CREDENZIALI);
			String utenteAttivo = null;

			// CICLO DI LOGIN
			while (utenteAttivo == null) {
				System.out.println("\n--- ACCESSO SISTEMA ---");
				String u = InputDati.leggiStringaNonVuota("Username: ");
				String p = InputDati.leggiStringaNonVuota("Password: ");

				if (login.isMasterAdmin(u, p)) {
					System.out.println("\n--- REGISTRAZIONE NUOVO CONFIGURATORE ---");
					String nuovoU;
					while (true) {
						nuovoU = InputDati.leggiStringaNonVuota("Inserisci Username personale: ");
						if (!login.esisteGia(nuovoU))
							break;
						System.out.println("Errore: username già in uso o riservato!");
					}
					String nuovoP = InputDati.leggiStringaNonVuota("Inserisci Password personale: ");

					login.registraNuovo(nuovoU, nuovoP);
					login.salva(FILE_CREDENZIALI);
					utenteAttivo = nuovoU;
					System.out.println("Configuratore '" + nuovoU + "' registrato con successo.\n");
				} else if (login.autentica(u, p)) {
					utenteAttivo = u;
				} else {
					System.out.println(">>> Errore: Credenziali errate. Riprova.");
				}
			}

			// Inizializzazione dati
			config.caricaOInizializzaCampiBase(FILE_CAMPI_BASE, sc);
			config.caricaDati(FILE_DATI_SISTEMA);

			// Avvio Menu
			eseguiMenu(config, login, utenteAttivo, sc);

		} catch (IOException e) {
			System.out.println("Errore critico durante l'accesso ai file: " + e.getMessage());
		}
	}

	private static void eseguiMenu(Configuratore conf, SessioneLogin log, String user, Scanner sc) throws IOException {
		boolean exit = false;
		while (!exit) {
			System.out.println("\n========================================");
			System.out.println("CONFIGURATORE ATTIVO: " + user);
			System.out.println("1. Visualizza Categorie e Campi");
			System.out.println("2. Gestione Categorie (Aggiungi/Rimuovi)");
			System.out.println("3. Gestione Campi Comuni (Aggiungi/Rimuovi/Modifica)");
			System.out.println("4. Gestione Campi Specifici di una Categoria");
			System.out.println("5. Crea Nuova Proposta (Salva in Bozza)");
			System.out.println("6. Pubblica una Bozza in Bacheca");
			System.out.println("7. Visualizza Bacheca per Categoria");
			System.out.println("8. Visualizza Intera Bacheca");
			System.out.println("0. Salva ed Esci");

			String scelta = InputDati.leggiStringaNonVuota("Scelta: ");
			switch (scelta) {
			case "0":
				conf.salvaDati(FILE_DATI_SISTEMA);
				log.salva(FILE_CREDENZIALI);
				System.out.println("Dati salvati correttamente.");
				System.out.println("NOTA: Eventuali proposte rimaste in bozza sono state distrutte.");
				exit = true;
				break;
			case "1":
				conf.getCategorie().forEach(c -> c.visualizza(conf.getCampiBase(), conf.getCampiComuni()));
				break;
			case "2":
				String s2 = InputDati.leggiStringaNonVuota("1. Aggiungi\n2. Rimuovi ");
				String nCat = InputDati.leggiStringaNonVuota("Nome Categoria: ");
				if (s2.equals("1"))
					conf.creaCategoria(nCat);
				else
					conf.rimuoviCategoria(nCat);
				break;
			case "3":
				String s3 = InputDati.leggiStringaNonVuota("1. Aggiungi\n2. Rimuovi\n3. Modifica Obbligatorietà: ");
				String nC = InputDati.leggiStringaNonVuota("Nome Campo Comune: ");
				if (s3.equals("1")) {
					conf.aggiungiCampoComune(nC, InputDati.leggiStringaNonVuota("Obbligatorio? (s/n): ").equalsIgnoreCase("s"));
				} else if (s3.equals("2"))
					conf.rimuoviCampoComune(nC);
				else {
					conf.cambiaObbligatorietaComune(nC, InputDati.leggiStringaNonVuota("Nuovo stato Obbligatorio? (s/n): ").equalsIgnoreCase("s"));
				}
				break;
			case "4":
				String catS = InputDati.leggiStringaNonVuota("Categoria: ");
				String s4 = InputDati.leggiStringaNonVuota("1. Aggiungi Specifico\n2. Rimuovi Specifico ");
				String nS = InputDati.leggiStringaNonVuota("Nome Campo: ");
				if (s4.equals("1")) {
					conf.aggiungiCampoSpecifico(catS, nS, InputDati.leggiStringaNonVuota("Obbligatorio? (s/n): ").equalsIgnoreCase("s"));
				} else
					conf.rimuoviCampoSpecifico(catS, nS);
				break;
			case "5":
				creaNuovaProposta(conf, sc);
				break;
			case "6":
				gestisciBozze(conf, sc);
				break;
			case "7":
				conf.visualizzaBachecaFiltrata(InputDati.leggiStringaNonVuota("Inserisci il nome della Categoria da visualizzare in bacheca: "));
				break;
			case "8":
				conf.visualizzaBachecaIntera();
				break;
			default:
				System.out.println("Opzione non valida.");
				break;
			}
		}
	}

	private static void creaNuovaProposta(Configuratore conf, Scanner sc) {
		System.out.println("\n--- CREAZIONE NUOVA PROPOSTA ---");
		String catNome = InputDati.leggiStringaNonVuota("Seleziona la Categoria dell'iniziativa: ");

		Categoria sel = null;
		for(Categoria c : conf.getCategorie()) {
			if(c.getNome().equalsIgnoreCase(catNome)) {
				sel = c;
				break;
			}
		}

		if (sel == null) {
			System.out.println("Errore: Categoria non trovata.");
			return;
		}

		Proposta p = new Proposta(sel);

		List<Campo> tutti = new ArrayList<>();
		tutti.addAll(conf.getCampiBase());
		tutti.addAll(conf.getCampiComuni());
		tutti.addAll(sel.getCampiSpecifici());

		System.out.println("Inserisci i valori richiesti (le date devono essere nel formato gg/mm/aaaa):");
		for (Campo c : tutti) {
			String val = InputDati.leggiStringaNonVuota("- " + c.getNome() + (c.isObbligatorio() ? "*" : "") + ": ");
			if (!val.isBlank()) p.inserisciValore(c.getNome(), val);
		}

		// Validazione e Salvataggio Volatile
		if (conf.validaProposta(p)) {
			conf.aggiungiBozza(p);
			System.out.println("\n>>> LA PROPOSTA È VALIDA!");
			System.out.println("È stata salvata temporaneamente in BOZZA (Stato: VALIDA).");
			System.out.println("Usa l'opzione 6 del menu per pubblicarla in Bacheca, altrimenti andrà persa alla chiusura.");
		} else {
			System.out.println("\n>>> ERRORE: La proposta non rispetta i vincoli temporali o mancano campi obbligatori.");
		}
	}

	private static void gestisciBozze(Configuratore conf, Scanner sc) {
		List<Proposta> bozze = conf.getBozze();
		
		if (bozze.isEmpty()) {
			System.out.println("\nNon ci sono bozze in attesa in questa sessione.");
			return;
		}

		System.out.println("\n--- BOZZE DA PUBBLICARE ---");
		for (int i = 0; i < bozze.size(); i++) {
			Proposta b = bozze.get(i);
			
			// Cerca di prendere il primo campo base per identificare la proposta (es. "Titolo")
			String identificativo = "N/A";
			if (!conf.getCampiBase().isEmpty()) {
				identificativo = b.getValore(conf.getCampiBase().get(0).getNome());
			}

			System.out.println("[" + (i + 1) + "] Categoria: " + b.getCategoria().getNome() + 
							   " | Identificativo: " + identificativo);
		}

		try {
			int scelta = Integer.parseInt(InputDati.leggiStringaNonVuota("\nInserisci il numero della bozza da pubblicare in bacheca (0 per annullare): "));
			if (scelta > 0 && scelta <= bozze.size()) {
				conf.pubblicaBozza(scelta - 1);
				System.out.println(">>> Bozza pubblicata con successo! Ora è visibile in Bacheca.");
			} else if (scelta != 0) {
				System.out.println("Numero non valido.");
			}
		} catch (NumberFormatException e) {
			System.out.println("Errore: devi inserire un numero valido.");
		}
	}
}