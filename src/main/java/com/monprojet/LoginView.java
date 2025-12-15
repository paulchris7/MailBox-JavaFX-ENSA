package com.monprojet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Properties;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Gère la vue et la logique de l'écran de connexion.
 * <p>
 * Cette classe est responsable de l'affichage du formulaire de connexion,
 * de la validation des identifiants de l'utilisateur auprès du serveur IMAP de Gmail,
 * et de la transition vers l'application principale en cas de succès.
 * </p>
 */
public class LoginView {

    private final Stage stage;
    private String sessionEmail;
    private String sessionPassword;

    /**
     * Construit la vue de connexion.
     *
     * @param stage Le stage principal de l'application JavaFX.
     */
    public LoginView(Stage stage) {
        this.stage = stage;
    }

    /**
     * Affiche l'écran de connexion.
     * <p>
     * Crée et configure tous les composants graphiques de la scène de connexion.
     * </p>
     */
    public void show() {
        VBox loginLayout = new VBox(15);
        loginLayout.setPadding(new Insets(40));
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setStyle("-fx-background-color: #f4f4f4;");

        Label lblTitre = new Label("MailBox Login");
        lblTitre.setFont(Font.font("System", FontWeight.BOLD, 24));
        lblTitre.setStyle("-fx-text-fill: #2c3e50;");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Votre adresse Gmail");

        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Mot de passe d'application (16 lettres)");

        Button btnConnect = new Button("Se connecter");
        btnConnect.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
        btnConnect.setPrefWidth(200);

        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red;");

        btnConnect.setOnAction(e -> handleLogin(txtEmail.getText(), txtPass.getText(), lblError, btnConnect));

        loginLayout.getChildren().addAll(lblTitre, new Label("Email :"), txtEmail, new Label("Mot de passe App :"), txtPass, btnConnect, lblError);
        Scene scene = new Scene(loginLayout, 400, 350);
        stage.setTitle("Authentification - MailBox");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Gère la logique de tentative de connexion.
     * <p>
     * Cette méthode est appelée lors du clic sur le bouton de connexion. Elle valide
     * les champs, puis lance un thread séparé pour vérifier les identifiants
     * sans geler l'interface utilisateur.
     * </p>
     *
     * @param email      L'adresse email saisie par l'utilisateur.
     * @param password   Le mot de passe d'application saisi.
     * @param lblError   Le label utilisé pour afficher les messages d'erreur.
     * @param btnConnect Le bouton de connexion, pour le désactiver pendant la vérification.
     */
    private void handleLogin(String email, String password, Label lblError, Button btnConnect) {
        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Veuillez remplir tous les champs.");
            return;
        }

        btnConnect.setText("Vérification...");
        btnConnect.setDisable(true);

        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.store.protocol", "imaps");

                Session session = Session.getInstance(props, null);
                Store store = session.getStore("imaps");

                store.connect("imap.gmail.com", email, password);
                store.close();

                javafx.application.Platform.runLater(() -> {
                    this.sessionEmail = email;
                    this.sessionPassword = password;
                    launchMainApplication();
                });

            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    lblError.setText("Connexion refusée. Vérifiez les identifiants.");
                    btnConnect.setText("Se connecter");
                    btnConnect.setDisable(false);
                });
            }
        }).start();
    }

    /**
     * Lance l'application principale après une connexion réussie.
     * <p>
     * Initialise les services nécessaires (GmailService, DBService) et
     * affiche la vue principale de la boîte de réception.
     * </p>
     */
    private void launchMainApplication() {
        DBService dbService = new DBService();
        GmailService gmailService = new GmailService(sessionEmail, sessionPassword);
        MailBoxView mailBoxView = new MailBoxView(stage, dbService, gmailService, sessionEmail);
        mailBoxView.show();
    }
}
