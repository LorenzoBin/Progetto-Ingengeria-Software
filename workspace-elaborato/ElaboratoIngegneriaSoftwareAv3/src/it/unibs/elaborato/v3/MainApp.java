package it.unibs.elaborato.v3;

import java.util.*;
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
			String ruoloAttivo = null;

			// --- CICLO DI ACCESSO (LOGIN / REGISTRAZIONE) ---
			while (utenteAttivo == null) {
				System.out.println("\n--- ACCESSO SISTEMA (V3) ---");
				System.out.println("1. Login (Configuratore o Fruitore)");
				System.out.println("2. Registrazione Nuovo Fruitore");
				System.out.print("Scelta: ");
				String s = sc.nextLine();

				if (s.equals("2")) {
					System.out.print("Scegli uno Username: ");
					String u = sc.nextLine();
					System.out.print("Scegli una Password: ");
					String p = sc.nextLine();

					if (login.registraFruitore(u, p)) {
						login.salva(FILE_CREDENZIALI);
						System.out.println("\n>>> Fruitore registrato con successo!");
						System.out.println(">>> Accesso automatico in corso...");
						// Accesso automatico
						utenteAttivo = u;
						ruoloAttivo = "FRUITORE";
					} else {
						System.out.println("Errore: Username già in uso o riservato!");
					}
				} else if (s.equals("1")) {
					System.out.print("Username: ");
					String u = sc.nextLine();
					System.out.print("Password: ");
					String p = sc.nextLine();

					// Controllo se è l'admin master che deve registrare un configuratore
					if (login.isMasterAdmin(u, p)) {
						System.out.println("\n--- REGISTRAZIONE CONFIGURATORE ---");
						System.out.print("Nuovo Username: ");
						String nuovoU = sc.nextLine();
						System.out.print("Nuova Password: ");
						String nuovoP = sc.nextLine();
						if (login.registraConfiguratore(nuovoU, nuovoP)) {
							login.salva(FILE_CREDENZIALI);
							utenteAttivo = nuovoU;
							ruoloAttivo = "CONFIGURATORE";
							System.out.println(">>> Configuratore registrato! Accesso in corso...");
						} else {
							System.out.println("Errore: Username già in uso!");
						}
					} else {
						// Autenticazione normale
						ruoloAttivo = login.autentica(u, p);
						if (ruoloAttivo != null) {
							utenteAttivo = u;
							System.out.println("\n>>> Accesso effettuato come " + ruoloAttivo);
						} else {
							System.out.println(">>> Errore: Credenziali errate o utente non trovato.");
						}
					}
				} else {
					System.out.println("Opzione non valida. Digita 1 o 2.");
				}
			}

			// --- CARICAMENTO DATI E MOTORE TEMPORALE ---
			// Passo il ruoloAttivo per evitare che un Fruitore configuri il sistema al
			// primo avvio
			config.caricaOInizializzaCampiBase(FILE_CAMPI_BASE, sc, ruoloAttivo);
			config.caricaDati(FILE_DATI_SISTEMA);

			// V3: La "Mezzanotte"! Aggiorna gli stati scaduti e invia notifiche al login
			config.aggiornaStatiEInviaNotifiche();

			// --- SMISTAMENTO AI MENU ---
			if ("CONFIGURATORE".equals(ruoloAttivo)) {
				eseguiMenuConfiguratore(config, login, utenteAttivo, sc);
			} else {
				eseguiMenuFruitore(config, login, utenteAttivo, sc);
			}

		} catch (IOException e) {
			System.out.println("Errore critico nei file di sistema: " + e.getMessage());
		}
	}

	// --- MENU CONFIGURATORE ---
	private static void eseguiMenuConfiguratore(Configuratore conf, SessioneLogin log, String user, Scanner sc)
			throws IOException {
		boolean exit = false;
		while (!exit) {
			System.out.println("\n--- CONFIGURATORE: " + user + " ---");
			System.out.println(
					"1. Visualizza Categorie\n2. Gestione Categorie\n3. Gestione Campi Comuni\n4. Gestione Campi Specifici");
			System.out.println(
					"5. Crea Nuova Proposta (Bozza)\n6. Pubblica Bozza\n7. Visualizza Bacheca Filtrata\n8. Visualizza Intera Bacheca\n0. Salva ed Esci");
			System.out.print("Scelta: ");
			String scelta = sc.nextLine();
			switch (scelta) {
			case "0":
				conf.salvaDati(FILE_DATI_SISTEMA);
				log.salva(FILE_CREDENZIALI);
				exit = true;
				break;
			case "1":
				conf.getCategorie().forEach(c -> c.visualizza(conf.getCampiBase(), conf.getCampiComuni()));
				break;
			case "2":
				System.out.print("1. Add 2. Rem: ");
				if (sc.nextLine().equals("1"))
					conf.creaCategoria(sc.nextLine());
				else
					conf.rimuoviCategoria(sc.nextLine());
				break;
			case "3":
				System.out.print("1. Add 2. Rem 3. Mod: ");
				String s3 = sc.nextLine();
				String nC = sc.nextLine();
				if (s3.equals("1"))
					conf.aggiungiCampoComune(nC, sc.nextLine().equals("s"));
				else if (s3.equals("2"))
					conf.rimuoviCampoComune(nC);
				else
					conf.cambiaObbligatorietaComune(nC, sc.nextLine().equals("s"));
				break;
			case "4":
				System.out.print("Cat: ");
				String cS = sc.nextLine();
				System.out.print("1. Add 2. Rem: ");
				String s4 = sc.nextLine();
				String nS = sc.nextLine();
				if (s4.equals("1"))
					conf.aggiungiCampoSpecifico(cS, nS, sc.nextLine().equals("s"));
				else
					conf.rimuoviCampoSpecifico(cS, nS);
				break;
			case "5":
				creaNuovaProposta(conf, sc);
				break;
			case "6":
				gestisciBozze(conf, sc);
				break;
			case "7":
				System.out.print("Cat: ");
				conf.visualizzaBachecaFiltrata(sc.nextLine());
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

	// --- MENU FRUITORE ---
	private static void eseguiMenuFruitore(Configuratore conf, SessioneLogin log, String user, Scanner sc)
			throws IOException {
		Fruitore io = conf.getFruitore(user);
		boolean exit = false;
		while (!exit) {
			System.out.println("\n--- FRUITORE: " + user + " ---");
			System.out.println("1. Visualizza Bacheca\n2. Iscriviti a una Proposta\n3. Spazio Personale (Notifiche: "
					+ io.getSpazioPersonale().size() + ")\n0. Salva ed Esci");
			System.out.print("Scelta: ");
			String scelta = sc.nextLine();
			switch (scelta) {
			case "0":
				conf.salvaDati(FILE_DATI_SISTEMA);
				log.salva(FILE_CREDENZIALI);
				exit = true;
				break;
			case "1":
				conf.visualizzaBachecaIntera();
				break;
			case "2":
				List<Proposta> aperte = conf.getBacheca().getAperte();
				if (aperte.isEmpty()) {
					System.out.println("Nessuna proposta aperta in Bacheca.");
					break;
				}

				System.out.println("\n--- PROPOSTE DISPONIBILI ---");
				for (int i = 0; i < aperte.size(); i++) {
					// Stampa da 1 a N
					System.out.println("[" + (i + 1) + "] " + aperte.get(i).getValore("Titolo") + " (Cat: "
							+ aperte.get(i).getCategoria().getNome() + ")");
				}
				System.out.print("Inserisci il numero a cui vuoi iscriverti (0 per annullare): ");
				try {
					int idx = Integer.parseInt(sc.nextLine());
					if (idx == 0) {
						System.out.println(">>> Operazione annullata. Ritorno al menu principale.");
					} else if (idx > 0 && idx <= aperte.size()) {
						// Riadattiamo l'indice (idx - 1)
						if (aperte.get(idx - 1).iscrivi(user)) {
							System.out.println(">>> Iscrizione effettuata con successo!");
						} else {
							System.out.println(
									">>> Errore: Sei già iscritto, oppure l'evento ha raggiunto il limite massimo di partecipanti!");
						}
					} else {
						System.out.println("Indice non valido.");
					}
				} catch (Exception e) {
					System.out.println("Valore inserito non valido.");
				}
				break;
			case "3":
				List<Notifica> not = io.getSpazioPersonale();
				if (not.isEmpty()) {
					System.out.println("Spazio personale vuoto.");
					break;
				}
				System.out.println("\n--- LE TUE NOTIFICHE ---");
				for (int i = 0; i < not.size(); i++)
					System.out.println("[" + i + "] " + not.get(i));
				System.out.print("\nVuoi cancellare una notifica? Inserisci il numero (o -1 per tornare al menu): ");
				try {
					int idx = Integer.parseInt(sc.nextLine());
					if (idx >= 0 && idx < not.size()) {
						io.rimuoviNotifica(idx);
						System.out.println("Notifica rimossa.");
					}
				} catch (Exception e) {
				}
				break;
			default:
				System.out.println("Opzione non valida.");
				break;
			}
		}
	}

	// --- METODI HELPER PER CONFIGURATORE ---
	private static void creaNuovaProposta(Configuratore conf, Scanner sc) {
		System.out.print("Categoria dell'iniziativa: ");
		Categoria sel = null;
		String catNome = sc.nextLine();
		for (Categoria c : conf.getCategorie())
			if (c.getNome().equalsIgnoreCase(catNome))
				sel = c;
		if (sel == null) {
			System.out.println("Categoria non trovata.");
			return;
		}
		Proposta p = new Proposta(sel);
		List<Campo> tutti = new ArrayList<>(conf.getCampiBase());
		tutti.addAll(conf.getCampiComuni());
		tutti.addAll(sel.getCampiSpecifici());
		for (Campo c : tutti) {
			System.out.print("- " + c.getNome() + (c.isObbligatorio() ? "*" : "") + ": ");
			String val = sc.nextLine();
			if (!val.isBlank())
				p.inserisciValore(c.getNome(), val);
		}
		if (conf.validaProposta(p)) {
			conf.aggiungiBozza(p);
			System.out.println("Salvata in BOZZA.");
		} else
			System.out.println("Errore di validazione date o campi mancanti.");
	}

	private static void gestisciBozze(Configuratore conf, Scanner sc) {
		List<Proposta> bozze = conf.getBozze();
		if (bozze.isEmpty()) {
			System.out.println("Nessuna bozza da pubblicare.");
			return;
		}

		System.out.println("\n--- BOZZE IN ATTESA ---");
		for (int i = 0; i < bozze.size(); i++) {
			String titolo = bozze.get(i).getValore("Titolo");
			if (titolo == null || titolo.isBlank())
				titolo = "Proposta senza titolo";
			String categoria = bozze.get(i).getCategoria().getNome();
			// Mostra sia il titolo dell'evento sia la categoria
			System.out.println("[" + (i + 1) + "] " + titolo + " (Cat: " + categoria + ")");
		}

		System.out.print("Seleziona il numero della proposta da pubblicare (0 per uscire): ");
		try {
			int scelta = Integer.parseInt(sc.nextLine());
			if (scelta > 0 && conf.pubblicaBozza(scelta - 1)) {
				System.out.println(">>> Proposta pubblicata in bacheca con successo!");
			}
		} catch (Exception e) {
			System.out.println("Valore non valido.");
		}
	}
}