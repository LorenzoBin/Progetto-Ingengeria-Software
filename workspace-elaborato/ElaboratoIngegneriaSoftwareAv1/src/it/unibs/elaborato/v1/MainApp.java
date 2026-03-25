package it.unibs.elaborato.v1;

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
			// Caricamento credenziali esistenti
			login.carica(FILE_CREDENZIALI);

			String utenteAttivo = null;

			// CICLO DI LOGIN: Continua a chiedere finché non è corretto
			while (utenteAttivo == null) {
				System.out.println("\n--- ACCESSO SISTEMA ---");
				System.out.print("User: ");
				String u = sc.nextLine();
				System.out.print("Pass: ");
				String p = sc.nextLine();

				// CASO A: Registrazione tramite Master Admin
				if (login.isMasterAdmin(u, p)) {
					System.out.println("\n--- REGISTRAZIONE NUOVO CONFIGURATORE ---");
					String nuovoU;
					while (true) {
						System.out.print("Inserisci Username personale: ");
						nuovoU = sc.nextLine();
						if (!login.esisteGia(nuovoU))
							break;
						System.out.println("Errore: username già in uso o riservato!");
					}
					System.out.print("Inserisci Password personale: ");
					String nuovoP = sc.nextLine();

					login.registraNuovo(nuovoU, nuovoP);
					login.salva(FILE_CREDENZIALI);
					utenteAttivo = nuovoU;
					System.out.println("Configuratore '" + nuovoU + "' registrato con successo.\n");
				}
				// CASO B: Accesso utente già registrato
				else if (login.autentica(u, p)) {
					utenteAttivo = u;
				}
				// CASO C: Credenziali errate
				else {
					System.out.println(">>> Errore: Credenziali errate. Riprova.");
				}
			}

			// Una volta usciti dal ciclo (utenteAttivo non è più null)
			// Inizializzazione dati sistema
			config.caricaOInizializzaCampiBase(FILE_CAMPI_BASE, sc);
			config.caricaDati(FILE_DATI_SISTEMA);

			// Avvio Menu Principale
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
			System.out.println("0. Salva ed Esci");
			System.out.print("Scelta: ");

			String scelta = sc.nextLine();
			switch (scelta) {
			case "0":
				conf.salvaDati(FILE_DATI_SISTEMA);
				log.salva(FILE_CREDENZIALI);
				System.out.println("Dati salvati correttamente. Arrivederci!");
				exit = true;
				break;
			case "1":
				conf.getCategorie().forEach(c -> c.visualizza(conf.getCampiBase(), conf.getCampiComuni()));
				break;
			case "2":
				System.out.print("1. Aggiungi\n2. Rimuovi: ");
				String s2 = sc.nextLine();
				System.out.print("Nome Categoria: ");
				String nCat = sc.nextLine();
				if (s2.equals("1"))
					conf.creaCategoria(nCat);
				else
					conf.rimuoviCategoria(nCat);
				break;
			case "3":
				System.out.print("1. Aggiungi\n2. Rimuovi\n3. Modifica Obbligatorietà: ");
				String s3 = sc.nextLine();
				System.out.print("Nome Campo Comune: ");
				String nC = sc.nextLine();
				if (s3.equals("1")) {
					System.out.print("Obbligatorio? (s/n): ");
					conf.aggiungiCampoComune(nC, sc.nextLine().equalsIgnoreCase("s"));
				} else if (s3.equals("2"))
					conf.rimuoviCampoComune(nC);
				else {
					System.out.print("Nuovo stato Obbligatorio? (s/n): ");
					conf.cambiaObbligatorietaComune(nC, sc.nextLine().equalsIgnoreCase("s"));
				}
				break;
			case "4":
				System.out.print("Categoria: ");
				String catS = sc.nextLine();
				System.out.print("1. Aggiungi Specifico\n2. Rimuovi Specifico: ");
				String s4 = sc.nextLine();
				System.out.print("Nome Campo: ");
				String nS = sc.nextLine();
				if (s4.equals("1")) {
					System.out.print("Obbligatorio? (s/n): ");
					conf.aggiungiCampoSpecifico(catS, nS, sc.nextLine().equalsIgnoreCase("s"));
				} else
					conf.rimuoviCampoSpecifico(catS, nS);
				break;
			default:
				System.out.println("Opzione non valida.");
				break;
			}
		}
	}
}