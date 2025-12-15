package com.monprojet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Fournit des services pour interagir avec la base de données des e-mails.
 * <p>
 * Cette classe gère toutes les opérations CRUD (Create, Read, Update, Delete)
 * pour les e-mails stockés dans une base de données MySQL locale. Elle encapsule
 * la logique de connexion et les requêtes SQL.
 * </p>
 */
public class DBService {
    
    /**
     * URL de connexion à la base de données.
     */
    private static final String URL = "jdbc:mysql://localhost:3306/mailbox_db";
    
    /**
     * Nom d'utilisateur pour la connexion à la base de données.
     */
    private static final String USER = "root";
    
    /**
     * Mot de passe pour la connexion à la base de données.
     */
    private static final String PASS = "";

    /**
     * Récupère la liste des e-mails pour un dossier spécifié.
     *
     * @param dossier Le nom du dossier (ex: "INBOX", "OUTBOX", "ENSA").
     * @return Une liste d'objets {@link Email} triés par date d'envoi décroissante.
     */
    public List<Email> getEmails(String dossier) {
        List<Email> liste = new ArrayList<>();
        String sql = "SELECT * FROM emails WHERE dossier = ? ORDER BY date_envoi DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dossier);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Email e = new Email(
                        rs.getInt("id"),
                        rs.getString("expediteur"),
                        rs.getString("destinataire"),
                        rs.getString("sujet"),
                        rs.getString("message"),
                        rs.getTimestamp("date_envoi"),
                        rs.getString("dossier")
                    );
                    liste.add(e);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des emails : " + e.getMessage());
        }
        return liste;
    }

    /**
     * Sauvegarde un nouvel e-mail dans la base de données.
     * La date d'envoi est automatiquement définie sur l'heure actuelle.
     *
     * @param email L'objet {@link Email} à sauvegarder.
     */
    public void saveEmail(Email email) {
        String sql = "INSERT INTO emails (expediteur, destinataire, sujet, message, dossier, date_envoi) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.getExpediteur());
            pstmt.setString(2, email.getDestinataire());
            pstmt.setString(3, email.getSujet());
            pstmt.setString(4, email.getMessage());
            pstmt.setString(5, email.getDossier());
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de l'email : " + e.getMessage());
        }
    }

    /**
     * Supprime un e-mail de la base de données en utilisant son identifiant.
     *
     * @param id L'identifiant unique de l'e-mail à supprimer.
     */
    public void deleteEmail(int id) {
        String sql = "DELETE FROM emails WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'email : " + e.getMessage());
        }
    }

    /**
     * Vérifie si un e-mail existe déjà dans la base de données.
     * <p>
     * La vérification est basée sur une combinaison unique de l'expéditeur, du sujet et de la date d'envoi
     * pour éviter les doublons lors de la synchronisation.
     * </p>
     *
     * @param expediteur L'adresse e-mail de l'expéditeur.
     * @param sujet      Le sujet de l'e-mail.
     * @param date       La date et l'heure d'envoi de l'e-mail.
     * @return {@code true} si un e-mail correspondant est trouvé, sinon {@code false}.
     */
    public boolean emailExiste(String expediteur, String sujet, Timestamp date) {
        String sql = "SELECT COUNT(*) FROM emails WHERE expediteur = ? AND sujet = ? AND date_envoi = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, expediteur);
            pstmt.setString(2, sujet);
            pstmt.setTimestamp(3, date);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'existence de l'email : " + e.getMessage());
        }
        return false;
    }
}
