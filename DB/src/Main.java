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
	 * Tämä metodi luo tietokannan jos sitä ei ole jo olemassa. 
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
	 * Tämä metodi luo käyttäjätaulukon jos sitä ei ole vielä olemassa. 
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
	 * Tämä metodi luo pääkäyttäjän jos käyttäjää ei ole vielä olemassa. 
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
	 * Tällä metodilla käyttäjät kirjautuvat sisään järjestelmään. 
	 */
	public static void kirjautuminen (){
		
		
		String url = "jdbc:sqlite:kanta.db";
		try (Connection conn = DriverManager.getConnection(url)) {
			System.out.println("Tervehdys! Syötä käyttäjänimi: ");
			String knimi = lukija.nextLine();
			System.out.println("Syötä salasana: ");
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
				System.out.println("Käyttäjänimi ja salasana oikein!");
				kayttajaTaso(knimi, taso);
			} else {
				System.out.println("Käyttäjänimi tai salasana väärin. Palaat alkuun.");
				kirjautuminen();
			}
		}/*try*/ catch (SQLException e) {
			System.out.println(e.getMessage());
		}//catch 
		
	}
	
	/**
	 * Tämä metodi määrittää käyttäjän oikeudet. Se ajetaan aina ennen valikkoon siirtymistä. 
	 * Bugi: Jostakin syystä komento 'if (taso == "admin")' ei toimi, vaikka tason arvo olisikin 'admin'. 
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
	 * Tämä metodi on pääkäyttäjän käyttöliittymä. 
	 * KESKENERÄINEN
	 * @param knimi
	 * @param taso
	 */
	public static void adminValikko (String knimi, String taso) {
		System.out.println("Tervetuloa pääkäyttäjä " + knimi + "! Valitse tehtävä: ");
		System.out.println("1. Selaa käyttäjiä			2. Katso käyttäjien töitä (ei käytössä)"); 
		System.out.println("3. Lisää uusi käyttäjä			4. Poista käyttäjä"); 
		System.out.println("5. Vaihda salasanaa			6. Kirjaudu ulos"); 
		int valinta = lukija.nextInt();
		if (valinta == 1) {
			kayttajaLista (knimi, taso);
		} else if (valinta == 3)
		{
			lisaaKayttaja (knimi, taso);
		} else if (valinta == 6) {
			System.out.println("Olet nyt kirjautunut ulos järjestelmästä " + knimi + ".");
			kirjautuminen();
		} else {
		
			System.out.println("Virheellinen valinta");
			adminValikko(knimi, taso);
		}
	}//adminValikko
	
	/**
	 * Tämä metodi on tavallisen käyttäjän käyttöliittymä. 
	 * @param knimi
	 * @param taso
	 */
	public static void userValikko(String knimi, String taso) {
		System.out.println("Tervetuloa käyttäjä " + knimi + "! Valitse tehtävä:");
		System.out.println("1. Selaa käyttäjiä			2. Muokkaa työtäsi (ei käytössä)");
		System.out.println("3. Vaihda salasana			4. Kirjaudu ulos");
		int valinta = lukija.nextInt();
		if (valinta == 1) {
			kayttajaLista(knimi, taso);
		} else if (valinta == 3) {
			vaihdaSalasana(knimi, taso);
		} else if (valinta == 4) {
			System.out.println("Olet nyt kirjautunut ulos järjestelmästä " + knimi + ".");
			kirjautuminen();
		} else {
			System.out.println("Virheellinen valinta");
			userValikko(knimi, taso);
		}
	}//userValikko
	
	
	/**
	 * Tämä metodi lisää uuden käyttäjän. 
	 * BUGI: parametri käyttäjänimi ei siirry muuttujaan. 
	 * KORJAUS: Ylimääräinen nextLine()-funktio ennen nimen valintaa jostakin syystä korjaa ongelman. 
	 * BUGI2: Käyttäjänimen varmistus juuttuu käyttäjänimen kysymiseen jos käyttäjä syöttää jo 
	 * käytössä olevan nimen, jos palautukseen käytetään metodin lisaaKayttaja() uudelleenajoa. 
	 * KORJAUS2: Toistaiseksi metodi lähettää käyttäjän takaisin päävalikkoon. 
	 * @param knimi
	 * @param taso
	 */
	public static void lisaaKayttaja(String knimi, String taso) {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "INSERT INTO kayttajat("
				+ "knimi, ssana, taso) "
				+ "VALUES(?, ?, 'user');";
		
		System.out.println("Syötä uusi käyttäjänimi: ");
		lukija.nextLine();
		String uusiKnimi = lukija.nextLine();
		if (varmistaKayttaja(uusiKnimi) == true) {
			System.out.println("Tämä käyttäjänimi on jo käytössä. Palaat alkuun");
			adminValikko(knimi, taso);
		} else {
			System.out.println("Syötä uuden käyttäjän salasana: ");
			lukija.nextLine();
			String uusiSsana= lukija.nextLine();
			try (Connection conn = DriverManager.getConnection(url) ) {
				PreparedStatement pstmt = conn.prepareStatement(sql);
			       pstmt.setString(1, uusiKnimi);
			       pstmt.setString(2, uusiSsana);
			       pstmt.executeUpdate(); 
			       System.out.println("Taulukko päivitetty");
			       System.out.println("Käyttäjä " + uusiKnimi + " luotu.");
			       kayttajaTaso(knimi, taso);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				}//catch
		}//else
	}//lisaaKayttaja
	
	/**
	 * Varmistaa onko lisättävä käyttäjänimi jo käytössä. 
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
	 * Tämä metodi näyttää listan kaikista käyttäjistä.  
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
	 * Tämä metodi antaa käyttäjän vaihtaa salasanansa. 
	 * KESKENERÄINEN
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
