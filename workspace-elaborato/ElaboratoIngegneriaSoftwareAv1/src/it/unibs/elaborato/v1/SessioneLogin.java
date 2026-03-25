package it.unibs.elaborato.v1;

import java.io.*;
import java.util.*;

public class SessioneLogin {
	private Map<String, String> utenti = new HashMap<>();
	private static final String ADMIN_MASTER = "admin";

	public boolean isMasterAdmin(String u, String p) {
		return ADMIN_MASTER.equals(u) && ADMIN_MASTER.equals(p);
	}

	public boolean autentica(String u, String p) {
		return utenti.containsKey(u) && utenti.get(u).equals(p);
	}

	// Ritorna false se lo username esiste già (univocità)
	public boolean registraNuovo(String u, String p) {
		if (esisteGia(u)) {
			return false;
		}
		utenti.put(u, p);
		return true;
	}

	public boolean esisteGia(String u) {
		// Lo username non può essere 'admin' né uno già presente
		return utenti.containsKey(u) || ADMIN_MASTER.equalsIgnoreCase(u);
	}

	public void salva(String path) throws IOException {
		try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
			for (Map.Entry<String, String> entry : utenti.entrySet()) {
				pw.println(entry.getKey() + ";" + entry.getValue());
			}
		}
	}

	public void carica(String path) throws IOException {
		File f = new File(path);
		if (!f.exists())
			return;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String l;
			while ((l = br.readLine()) != null) {
				String[] p = l.split(";");
				if (p.length >= 2)
					utenti.put(p[0], p[1]);
			}
		}
	}
}