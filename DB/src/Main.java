import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Scanner;

public class Main {
	Connection conn = null; 
	private static final Scanner lukija = new Scanner(System.in);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		tietokanta();
		taulu();
		paakayttaja();
		kirjautuminen();
		
	}//main
	
	
	/**
	 * T�m� metodi luo tietokannan jos sit� ei ole jo olemassa. 
	 */
	
	public static void tietokanta() {
		String url = "jdbc:sqlite:kanta.db";
		try (Connection conn = DriverManager.getConnection(url)) {
			if (conn != null) {
				DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Ajuri on " + meta.getDriverName());
                System.out.println("Tietokanta on luotu.");
			}//if
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}//catch 
	}//tietokanta
	
	/**
	 * T�m� metodi luo k�ytt�j�taulukon jos sit� ei ole viel� olemassa. 
	 */
	public static void taulu () {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "CREATE TABLE IF NOT EXISTS kayttajat (\n"
                + " id integer PRIMARY KEY,\n"
                + " knimi text NOT NULL,\n"
                + " ssana text NOT NULL,\n"
                + " taso text NOT NULL);";
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
            System.out.println("Taulu on luotu.");
        }/*try*/ catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
	}//taulu
	
	/**
	 * T�m� metodi luo p��k�ytt�j�n jos k�ytt�j�� ei ole viel� olemassa. 
	 */
	public static void paakayttaja () {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "INSERT OR IGNORE INTO kayttajat \n"
				+ " (id, knimi, ssana, taso) \n" 
				+ " VALUES (1, 'admin', 'admin', 'admin');";
		try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
            System.out.println("Admin on luotu.");
        }/*try*/ catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
	}//paakayttaja
	
	/**
	 * T�ll� metodilla k�ytt�j�t kirjautuvat sis��n j�rjestelm��n. 
	 */
	public static void kirjautuminen (){
		
		
		String url = "jdbc:sqlite:kanta.db";
		try (Connection conn = DriverManager.getConnection(url)) {
			System.out.println("Tervehdys! Sy�t� k�ytt�j�nimi: ");
			String knimi = lukija.nextLine();
			System.out.println("Sy�t� salasana: ");
			String ssana = lukija.nextLine();
			
			String query = "SELECT * FROM kayttajat WHERE knimi = ? AND ssana = ?";
			System.out.println("Query muodostettu.");
			PreparedStatement pst = conn.prepareStatement(query);
			System.out.println("Prepared statement luotu.");
			pst.setString(1, knimi);
			pst.setString(2, ssana);
			System.out.println("Stringit liitetty.");
			ResultSet rs = pst.executeQuery(); 
			System.out.println("Kysely suoritettu.");
			String taso = rs.getString("taso"); 
			
			int oikein = 0; 
			while(rs.next()) {
				oikein++; 
			}
			if (oikein == 1) {
				System.out.println("K�ytt�j�nimi ja salasana oikein!");
				kayttajaTaso(knimi, taso);
			} else {
				System.out.println("K�ytt�j�nimi tai salasana v��rin. Palaat alkuun.");
				kirjautuminen();
			}
		}/*try*/ catch (SQLException e) {
			System.out.println(e.getMessage());
		}//catch 
		
	}
	
	/**
	 * T�m� metodi m��ritt�� k�ytt�j�n oikeudet. Se ajetaan aina ennen valikkoon siirtymist�. 
	 * Bugi: Jostakin syyst� komento 'if (taso == "admin")' ei toimi, vaikka tason arvo olisikin 'admin'. 
	 * Bugi kierretty toistaiseksi contains()-funktiolla. 
	 * @param knimi
	 * @param taso
	 */
	public static void kayttajaTaso (String knimi, String taso) {
		System.out.println("Taso on " + taso);
		if (taso.contains("admin") ) {
			adminValikko(knimi, taso);
		} else {
			userValikko(knimi, taso);
		}
	}//kayttajaTaso
	
	/**
	 * T�m� metodi on p��k�ytt�j�n k�ytt�liittym�. 
	 * KESKENER�INEN
	 * @param knimi
	 * @param taso
	 */
	public static void adminValikko (String knimi, String taso) {
		System.out.println("Tervetuloa p��k�ytt�j� " + knimi + "! Valitse teht�v�: ");
		System.out.println("1. Selaa k�ytt�ji�			2. Katso k�ytt�jien t�it� (ei k�yt�ss�)"); 
		System.out.println("3. Lis�� uusi k�ytt�j�			4. Poista k�ytt�j�"); 
		System.out.println("5. Vaihda salasanaa			6. Kirjaudu ulos"); 
		int valinta = lukija.nextInt();
		if (valinta == 1) {
			kayttajaLista (knimi, taso);
		} else if (valinta == 3)
		{
			lisaaKayttaja (knimi, taso);
		} else if (valinta == 6) {
			System.out.println("Olet nyt kirjautunut ulos j�rjestelm�st� " + knimi + ".");
			kirjautuminen();
		} else {
		
			System.out.println("Virheellinen valinta");
			adminValikko(knimi, taso);
		}
	}//adminValikko
	
	/**
	 * T�m� metodi on tavallisen k�ytt�j�n k�ytt�liittym�. 
	 * @param knimi
	 * @param taso
	 */
	public static void userValikko(String knimi, String taso) {
		System.out.println("Tervetuloa k�ytt�j� " + knimi + "! Valitse teht�v�:");
		System.out.println("1. Selaa k�ytt�ji�			2. Muokkaa ty�t�si (ei k�yt�ss�)");
		System.out.println("3. Vaihda salasana			4. Kirjaudu ulos");
		int valinta = lukija.nextInt();
		if (valinta == 1) {
			kayttajaLista(knimi, taso);
		} else if (valinta == 3) {
			vaihdaSalasana(knimi, taso);
		} else if (valinta == 4) {
			System.out.println("Olet nyt kirjautunut ulos j�rjestelm�st� " + knimi + ".");
			kirjautuminen();
		} else {
			System.out.println("Virheellinen valinta");
			userValikko(knimi, taso);
		}
	}//userValikko
	
	
	/**
	 * T�m� metodi lis�� uuden k�ytt�j�n. 
	 * BUGI: parametri k�ytt�j�nimi ei siirry muuttujaan. 
	 * KORJAUS: Ylim��r�inen nextLine()-funktio ennen nimen valintaa jostakin syyst� korjaa ongelman. 
	 * BUGI2: K�ytt�j�nimen varmistus juuttuu k�ytt�j�nimen kysymiseen jos k�ytt�j� sy�tt�� jo 
	 * k�yt�ss� olevan nimen, jos palautukseen k�ytet��n metodin lisaaKayttaja() uudelleenajoa. 
	 * KORJAUS2: Toistaiseksi metodi l�hett�� k�ytt�j�n takaisin p��valikkoon. 
	 * @param knimi
	 * @param taso
	 */
	public static void lisaaKayttaja(String knimi, String taso) {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "INSERT INTO kayttajat("
				+ "knimi, ssana, taso) "
				+ "VALUES(?, ?, 'user');";
		
		System.out.println("Sy�t� uusi k�ytt�j�nimi: ");
		lukija.nextLine();
		String uusiKnimi = lukija.nextLine();
		if (varmistaKayttaja(uusiKnimi) == true) {
			System.out.println("T�m� k�ytt�j�nimi on jo k�yt�ss�. Palaat alkuun");
			adminValikko(knimi, taso);
		} else {
			System.out.println("Sy�t� uuden k�ytt�j�n salasana: ");
			lukija.nextLine();
			String uusiSsana= lukija.nextLine();
			try (Connection conn = DriverManager.getConnection(url) ) {
				PreparedStatement pstmt = conn.prepareStatement(sql);
			       pstmt.setString(1, uusiKnimi);
			       pstmt.setString(2, uusiSsana);
			       pstmt.executeUpdate(); 
			       System.out.println("Taulukko p�ivitetty");
			       System.out.println("K�ytt�j� " + uusiKnimi + " luotu.");
			       kayttajaTaso(knimi, taso);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				}//catch
		}//else
	}//lisaaKayttaja
	
	/**
	 * Varmistaa onko lis�tt�v� k�ytt�j�nimi jo k�yt�ss�. 
	 * @param uusiKnimi
	 */
	public static boolean varmistaKayttaja (String uusiKnimi) {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "SELECT knimi FROM kayttajat WHERE knimi = ?";
		try (Connection conn = DriverManager.getConnection(url) ) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, uusiKnimi);
			ResultSet rs = pstmt.executeQuery();
			int oikein = 0; 
			while (rs.next()) {
				oikein++;
			}//while
			if (oikein >= 1) {
				return true;
			} else {
				return false; 
			}//else
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return true;
		}//catch	
	}
	
	/**
	 * T�m� metodi n�ytt�� listan kaikista k�ytt�jist�.  
	 * @param knimi
	 * @param taso
	 */
	public static void kayttajaLista(String knimi, String taso) {
		  String url = "jdbc:sqlite:kanta.db";
		  String sql = "SELECT id, knimi, taso FROM kayttajat";
		  try {
	            Connection conn = DriverManager.getConnection(url);
	            Statement stmt = conn.createStatement();
	            ResultSet rs = stmt.executeQuery(sql);
	            while (rs.next()) {
	            	System.out.println(rs.getInt("id") + "\t" +
	            						rs.getString("knimi") + "\t" +
	            						rs.getString("taso"));
	            }//while
	            kayttajaTaso(knimi, taso);
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }//catch
	}//kayttajaLista
	
	
	
	/**
	 * T�m� metodi antaa k�ytt�j�n vaihtaa salasanansa. 
	 * KESKENER�INEN
	 * @param knimi
	 * @param ssana
	 */
	public static void vaihdaSalasana (String knimi, String ssana) {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "UPDATE kayttajat SET ssana = ? "
						+ "WHERE knimi = ?";
		try {
			Connection conn = DriverManager.getConnection(url);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, lukija.nextLine());
			pstmt.setString(2, knimi);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}//catch
	}//vaihdaSalasana
	

	
}//Main
