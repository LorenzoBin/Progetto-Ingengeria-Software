package it.unibs.elaborato.v3;

import java.io.*;
import java.util.*;

public class SessioneLogin {
	private Map<String, String> configuratori = new HashMap<>();
	private Map<String, String> fruitori = new HashMap<>();
	private static final String ADMIN_MASTER = "admin";

	public boolean isMasterAdmin(String u, String p) {
		return ADMIN_MASTER.equals(u) && ADMIN_MASTER.equals(p);
	}

	// Ritorna il ruolo ("CONFIGURATORE", "FRUITORE" o null se errato)
	public String autentica(String u, String p) {
		if (configuratori.containsKey(u) && configuratori.get(u).equals(p))
			return "CONFIGURATORE";
		if (fruitori.containsKey(u) && fruitori.get(u).equals(p))
			return "FRUITORE";
		return null;
	}

	public boolean esisteGia(String u) {
		return configuratori.containsKey(u) || fruitori.containsKey(u) || ADMIN_MASTER.equalsIgnoreCase(u);
	}

	public boolean registraConfiguratore(String u, String p) {
		if (esisteGia(u))
			return false;
		configuratori.put(u, p);
		return true;
	}

	public boolean registraFruitore(String u, String p) {
		if (esisteGia(u))
			return false;
		fruitori.put(u, p);
		return true;
	}

	public void salva(String path) throws IOException {
		try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
			pw.println("CONFIGURATORI");
			for (Map.Entry<String, String> e : configuratori.entrySet())
				pw.println(e.getKey() + ";" + e.getValue());
			pw.println("FRUITORI");
			for (Map.Entry<String, String> e : fruitori.entrySet())
				pw.println(e.getKey() + ";" + e.getValue());
		}
	}

	public void carica(String path) throws IOException {
		File f = new File(path);
		if (!f.exists())
			return;
		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String l;
			String sez = "CONFIGURATORI"; // Default essenziale per vecchi file della V2
			while ((l = br.readLine()) != null) {
				if (l.equals("CONFIGURATORI") || l.equals("FRUITORI")) {
					sez = l;
					continue;
				}
				String[] p = l.split(";");
				if (p.length >= 2) {
					if (sez.equals("CONFIGURATORI"))
						configuratori.put(p[0], p[1]);
					else
						fruitori.put(p[0], p[1]);
				}
			}
		}
	}
}