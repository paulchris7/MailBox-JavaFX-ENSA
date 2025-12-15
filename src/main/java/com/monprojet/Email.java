package com.monprojet;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Représente un e-mail.
 * <p>
 * Cette classe est un modèle de données (POJO) qui encapsule toutes les
 * informations relatives à un e-mail, telles que l'expéditeur, le destinataire,
 * le sujet, le contenu, la date d'envoi et le dossier de stockage.
 * </p>
 */
public class Email {

    private final int id;
    private final String expediteur;
    private final String destinataire;
    private final String sujet;
    private final String message;
    private final Timestamp date;
    private final String dossier;

    /**
     * Construit une nouvelle instance d'Email.
     *
     * @param id           L'identifiant unique de l'e-mail (généralement depuis la base de données).
     * @param expediteur   L'adresse e-mail de l'expéditeur.
     * @param destinataire L'adresse e-mail du destinataire.
     * @param sujet        Le sujet de l'e-mail.
     * @param message      Le contenu textuel de l'e-mail.
     * @param date         La date et l'heure d'envoi.
     * @param dossier      Le dossier où l'e-mail est classé (ex: "INBOX", "OUTBOX").
     */
    public Email(int id, String expediteur, String destinataire, String sujet, String message, Timestamp date, String dossier) {
        this.id = id;
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.sujet = sujet;
        this.message = message;
        this.date = date;
        this.dossier = dossier;
    }

    /**
     * Retourne une représentation textuelle concise de l'e-mail pour l'affichage
     * dans les listes.
     *
     * @return Une chaîne de caractères formatée (ex: "2023-12-25 10:30 | expediteur@test.com : Sujet de l'email").
     */
    @Override
    public String toString() {
        if (date == null) {
            return "Date inconnue | " + expediteur + " : " + sujet;
        }
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
        return formattedDate + " | " + expediteur + " : " + sujet;
    }

    // --- Getters ---

    /**
     * @return L'identifiant unique de l'e-mail.
     */
    public int getId() { return id; }

    /**
     * @return La date et l'heure d'envoi de l'e-mail.
     */
    public Timestamp getDate() { return date; }

    /**
     * @return Le sujet de l'e-mail.
     */
    public String getSujet() { return sujet; }

    /**
     * @return Le contenu (corps) de l'e-mail.
     */
    public String getMessage() { return message; }

    /**
     * @return L'adresse e-mail de l'expéditeur.
     */
    public String getExpediteur() { return expediteur; }

    /**
     * @return Le dossier de classement de l'e-mail.
     */
    public String getDossier() { return dossier; }

    /**
     * @return L'adresse e-mail du destinataire.
     */
    public String getDestinataire() { return destinataire; }
}
