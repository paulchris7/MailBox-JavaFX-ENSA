package com.monprojet;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Fournit des services pour interagir avec les serveurs Gmail (SMTP et IMAP).
 * <p>
 * Cette classe gère l'envoi d'e-mails via le protocole SMTP et la réception
 * des e-mails depuis la boîte de réception (INBOX) via le protocole IMAP.
 * Elle nécessite les identifiants de l'utilisateur (e-mail et mot de passe d'application)
 * pour s'authentifier auprès des serveurs de Google.
 * </p>
 */
public class GmailService {

    private final String userEmail;
    private final String userPassword;

    /**
     * Construit une instance du service Gmail.
     *
     * @param email    L'adresse e-mail de l'utilisateur.
     * @param password Le mot de passe d'application à 16 caractères généré pour cette application.
     */
    public GmailService(String email, String password) {
        this.userEmail = email;
        this.userPassword = password;
    }

    /**
     * Envoie un e-mail via le serveur SMTP de Gmail.
     *
     * @param destinataire L'adresse e-mail du destinataire.
     * @param sujet        Le sujet de l'e-mail.
     * @param contenu      Le corps du message au format texte brut.
     */
    public void envoyerEmail(String destinataire, String sujet, String contenu) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userEmail, userPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + destinataire);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    /**
     * Récupère les 20 derniers e-mails de la boîte de réception (INBOX) via IMAP.
     *
     * @return Une liste d'objets {@link Email} représentant les messages récupérés.
     */
    public List<Email> recupererInbox() {
        List<Email> emailsRecus = new ArrayList<>();
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");

        try (Store store = Session.getInstance(props, null).getStore("imaps")) {
            store.connect("imap.gmail.com", userEmail, userPassword);

            try (Folder inbox = store.getFolder("INBOX")) {
                inbox.open(Folder.READ_ONLY);

                int totalMessages = inbox.getMessageCount();
                int start = Math.max(1, totalMessages - 19);
                Message[] messages = inbox.getMessages(start, totalMessages);

                System.out.println("Récupération de " + messages.length + " emails depuis Gmail...");

                for (int i = messages.length - 1; i >= 0; i--) {
                    Message msg = messages[i];
                    String expediteur = InternetAddress.toString(msg.getFrom());
                    String sujet = msg.getSubject();
                    String contenu = getTextFromMessage(msg);
                    Timestamp date = new Timestamp(msg.getSentDate().getTime());

                    Email email = new Email(0, expediteur, userEmail, sujet, contenu, date, "INBOX");
                    emailsRecus.add(email);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des emails : " + e.getMessage());
        }
        return emailsRecus;
    }

    /**
     * Extrait le contenu textuel d'un message, qu'il soit en texte brut ou en multipart.
     *
     * @param message Le message à traiter.
     * @return Le contenu textuel du message.
     * @throws MessagingException Si une erreur survient lors de l'accès au contenu.
     * @throws IOException        Si une erreur d'entrée/sortie survient.
     */
    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }
        if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "Contenu non supporté.";
    }

    /**
     * Parcourt un {@link MimeMultipart} pour en extraire le contenu textuel.
     * <p>
     * Privilégie la partie "text/plain". Si non disponible, utilise la partie
     * "text/html" en supprimant les balises HTML de manière basique.
     * </p>
     *
     * @param mimeMultipart Le contenu multipart à analyser.
     * @return Le texte extrait.
     * @throws MessagingException Si une erreur survient lors de l'accès aux parties.
     * @throws IOException        Si une erreur d'entrée/sortie survient.
     */
    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                // Privilégier le texte brut s'il est disponible
                return bodyPart.getContent().toString();
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                // Solution simple pour enlever les balises HTML sans dépendance externe
                result.append(html.replaceAll("<[^>]*>", ""));
            }
        }
        return result.toString();
    }
}
