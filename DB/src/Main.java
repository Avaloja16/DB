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
	 * T‰m‰ metodi luo tietokannan jos sit‰ ei ole jo olemassa. 
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
	 * T‰m‰ metodi luo k‰ytt‰j‰taulukon jos sit‰ ei ole viel‰ olemassa. 
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
            stmt.close();
        }/*try*/ catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
	}//taulu
	
	/**
	 * T‰m‰ metodi luo p‰‰k‰ytt‰j‰n jos k‰ytt‰j‰‰ ei ole viel‰ olemassa. 
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
            stmt.close();
        }/*try*/ catch (SQLException e) {
            System.out.println(e.getMessage());
        }//catch
	}//paakayttaja
	
	/**
	 * T‰ll‰ metodilla k‰ytt‰j‰t kirjautuvat sis‰‰n j‰rjestelm‰‰n. 
	 */
	public static void kirjautuminen (){
		
		
		String url = "jdbc:sqlite:kanta.db";
		try (Connection conn = DriverManager.getConnection(url)) {
			System.out.println("Tervehdys! Syˆt‰ k‰ytt‰j‰nimi: ");
			String knimi = lukija.nextLine();
			System.out.println("Syˆt‰ salasana: ");
			String ssana = lukija.nextLine();
			
			String query = "SELECT * FROM kayttajat WHERE knimi = ? AND ssana = ?";
			/* System.out.println("Query muodostettu."); */
			PreparedStatement pst = conn.prepareStatement(query);
			/* System.out.println("Prepared statement luotu."); */
			pst.setString(1, knimi);
			pst.setString(2, ssana);
			/* System.out.println("Stringit liitetty."); */ 
			ResultSet rs = pst.executeQuery(); 
			/* System.out.println("Kysely suoritettu."); */ 
			/*String taso = rs.getString("taso"); */
			int oikein = 0; 
			while(rs.next()) {
				oikein++; 
				System.out.println(oikein);
				if (oikein > 0) {
					String taso = rs.getString("taso"); 
					System.out.println("K‰ytt‰j‰nimi ja salasana oikein!");
					pst.close();
					rs.close();
					kayttajaTaso(knimi, taso);
				} else {
					System.out.println("K‰ytt‰j‰nimi tai salasana v‰‰rin. Palaat alkuun.");
					pst.close();
					rs.close();
					kirjautuminen();
					}//else
				}//while
			/*
			if (oikein == 1) {
				System.out.println("K‰ytt‰j‰nimi ja salasana oikein!");
				kayttajaTaso(knimi, taso);
			} else {
				System.out.println("K‰ytt‰j‰nimi tai salasana v‰‰rin. Palaat alkuun.");
				kirjautuminen();
			}
			*/
		}/*try*/ catch (SQLException e) {
			System.out.println(e.getMessage());
		}//catch 
	}//kirjautuminen
	
	/**
	 * T‰m‰ metodi m‰‰ritt‰‰ k‰ytt‰j‰n oikeudet. Se ajetaan aina ennen valikkoon siirtymist‰. 
	 * Bugi: Jostakin syyst‰ komento 'if (taso == "admin")' ei toimi, vaikka tason arvo olisikin 'admin'. 
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
	 * T‰m‰ metodi on p‰‰k‰ytt‰j‰n k‰yttˆliittym‰. 
	 * KESKENERƒINEN
	 * @param knimi
	 * @param taso
	 */
	public static void adminValikko (String knimi, String taso) {
		System.out.println("Tervetuloa p‰‰k‰ytt‰j‰ " + knimi + "! Valitse teht‰v‰: ");
		System.out.println("1. Selaa k‰ytt‰ji‰			2. Katso k‰ytt‰jien tˆit‰ (ei k‰ytˆss‰)"); 
		System.out.println("3. Lis‰‰ uusi k‰ytt‰j‰			4. Poista k‰ytt‰j‰"); 
		System.out.println("5. Vaihda salasanaa			6. Kirjaudu ulos"); 
		int valinta = lukija.nextInt();
		if (valinta == 1) {
			kayttajaLista (knimi, taso);
		} else if (valinta == 3)
		{
			lisaaKayttaja (knimi, taso);
		} else if (valinta == 4) {
			poistaKayttaja(knimi, taso);
		} else if (valinta == 6) {
			System.out.println("Olet nyt kirjautunut ulos j‰rjestelm‰st‰ " + knimi + ".");
			lukija.nextLine();
			kirjautuminen();
			
		} else {
		
			System.out.println("Virheellinen valinta");
			adminValikko(knimi, taso);
		}
	}//adminValikko
	
	/**
	 * T‰m‰ metodi on tavallisen k‰ytt‰j‰n k‰yttˆliittym‰. 
	 * @param knimi
	 * @param taso
	 */
	public static void userValikko(String knimi, String taso) {
		System.out.println("Tervetuloa k‰ytt‰j‰ " + knimi + "! Valitse teht‰v‰:");
		System.out.println("1. Selaa k‰ytt‰ji‰			2. Muokkaa tyˆt‰si (ei k‰ytˆss‰)");
		System.out.println("3. Vaihda salasana			4. Kirjaudu ulos");
		int valinta = lukija.nextInt();
		if (valinta == 1) {
			kayttajaLista(knimi, taso);
		} else if (valinta == 3) {
			vaihdaSalasana(knimi, taso);
		} else if (valinta == 4) {
			System.out.println("Olet nyt kirjautunut ulos j‰rjestelm‰st‰ " + knimi + ".");
			lukija.nextLine();
			kirjautuminen();
		} else {
			System.out.println("Virheellinen valinta");
			userValikko(knimi, taso);
		}
	}//userValikko
	
	
	/**
	 * T‰m‰ metodi lis‰‰ uuden k‰ytt‰j‰n. 
	 * BUGI: parametri k‰ytt‰j‰nimi ei siirry muuttujaan. 
	 * KORJAUS: Ylim‰‰r‰inen nextLine()-funktio ennen nimen valintaa jostakin syyst‰ korjaa ongelman. 
	 * BUGI2: K‰ytt‰j‰nimen varmistus juuttuu k‰ytt‰j‰nimen kysymiseen jos k‰ytt‰j‰ syˆtt‰‰ jo 
	 * k‰ytˆss‰ olevan nimen, jos palautukseen k‰ytet‰‰n metodin lisaaKayttaja() uudelleenajoa. 
	 * KORJAUS2: Toistaiseksi metodi l‰hett‰‰ k‰ytt‰j‰n takaisin p‰‰valikkoon. 
	 * @param knimi
	 * @param taso
	 */
	public static void lisaaKayttaja(String knimi, String taso) {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "INSERT INTO kayttajat("
				+ "knimi, ssana, taso) "
				+ "VALUES(?, ?, 'user');";
		
		System.out.println("Syˆt‰ uusi k‰ytt‰j‰nimi: ");
		lukija.nextLine();
		String uusiKnimi = lukija.nextLine();
		
		if (varmistaKayttaja(uusiKnimi) == true) {
			System.out.println("T‰m‰ k‰ytt‰j‰nimi on jo k‰ytˆss‰. Palaat alkuun");
			adminValikko(knimi, taso);
		} else {
			System.out.println("Syˆt‰ uuden k‰ytt‰j‰n salasana: ");
			
			String uusiSsana= lukija.nextLine();
			try (Connection conn = DriverManager.getConnection(url) ) {
				PreparedStatement pstmt = conn.prepareStatement(sql);
			       pstmt.setString(1, uusiKnimi);
			       pstmt.setString(2, uusiSsana);
			       pstmt.executeUpdate(); 
			       System.out.println("Taulukko p‰ivitetty");
			       System.out.println("K‰ytt‰j‰ " + uusiKnimi + " luotu.");
			       pstmt.close();
			       kayttajaTaso(knimi, taso);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
				}//catch
		}//else
	}//lisaaKayttaja
	
	/**
	 * Varmistaa onko lis‰tt‰v‰ k‰ytt‰j‰nimi jo k‰ytˆss‰. 
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
				pstmt.close();
				rs.close();
				return true;
			} else {
				pstmt.close();
				rs.close();
				return false; 
			}//else
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return true;
		}//catch	
	}//varmistaKayttaja
	
	/**
	 * T‰m‰ metodi n‰ytt‰‰ listan kaikista k‰ytt‰jist‰.  
	 * @param knimi
	 * @param taso
	 */
	public static void kayttajaLista(String knimi, String taso) {
		  String url = "jdbc:sqlite:kanta.db";
		  String sql = "SELECT id, knimi, ssana, taso FROM kayttajat";
		  try {
	            Connection conn = DriverManager.getConnection(url);
	            Statement stmt = conn.createStatement();
	            ResultSet rs = stmt.executeQuery(sql);
	            while (rs.next()) {
	            	System.out.println(rs.getInt("id") + "\t" +
	            						rs.getString("knimi") + "\t" +
	            						rs.getString("taso"));
	            }//while
	            stmt.close();
				rs.close();
	            kayttajaTaso(knimi, taso);
	        } catch (SQLException e) {
	            System.out.println(e.getMessage());
	        }//catch
	}//kayttajaLista
	
	
	
	/**
	 * T‰m‰ metodi antaa k‰ytt‰j‰n vaihtaa salasanansa. 
	 * @param knimi
	 * @param taso
	 */
	public static void vaihdaSalasana (String knimi, String taso) {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "UPDATE kayttajat SET ssana = ? "
						+ "WHERE knimi = ?";
		try (Connection conn = DriverManager.getConnection(url)) {
			PreparedStatement pstmt = conn.prepareStatement(sql);
			System.out.println("Kirjoita uusi salasana:");
			lukija.nextLine();
			String uusiSsana = lukija.nextLine();
			pstmt.setString(1, uusiSsana);
			pstmt.setString(2, knimi);
			pstmt.executeUpdate();
			System.out.println("Salasana on p‰ivitetty.");
			pstmt.close();
			kayttajaTaso(knimi, taso);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}//catch
	}//vaihdaSalasana
	
	
	/**
	 * T‰m‰ metodi poistaa nimetyn k‰ytt‰j‰n. 
	 * @param knimi
	 * @param taso
	 */
	public static void poistaKayttaja (String knimi, String taso) {
		String url = "jdbc:sqlite:kanta.db";
		String sql = "DELETE FROM kayttajat WHERE knimi = ?";
		try (Connection conn = DriverManager.getConnection(url)) {
			System.out.println("Kirjoita poistettavan k‰ytt‰j‰n nimi: ");
			lukija.nextLine();
			String pKnimi = lukija.nextLine();
			if (varmistaKayttaja(pKnimi) == true) {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, pKnimi);
				pstmt.executeUpdate();
				pstmt.close();
				System.out.println("K‰ytt‰j‰ " + pKnimi + " on poistettu.");
				kayttajaTaso(knimi, taso);
				
			} else {
				System.out.println("K‰ytt‰j‰‰ " + pKnimi + " ei ole olemassa. Palaat alkuun.");
				kayttajaTaso(knimi, taso);
			}//else
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}//catch
	}//poistaKayttaja 
	
}//Main
