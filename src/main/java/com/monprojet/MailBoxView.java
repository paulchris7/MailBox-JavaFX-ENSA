package com.monprojet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Gère la vue principale de la boîte de réception après la connexion.
 * <p>
 * Cette classe est responsable de l'affichage de l'interface principale,
 * qui inclut la liste des e-mails, le volet de détails, la barre de recherche
 * et les actions de gestion des e-mails (nouveau, supprimer, changer de dossier).
 * </p>
 */
public class MailBoxView {

    private final Stage stage;
    private final DBService dbService;
    private final GmailService gmailService;
    private final String sessionEmail;

    private final ObservableList<Email> masterData = FXCollections.observableArrayList();
    private final FilteredList<Email> filteredData = new FilteredList<>(masterData, p -> true);

    private Label titleLabel;
    private TextField searchField;
    private VBox detailPane;
    private Label lblSujetDetail;
    private Label lblExpediteurDetail;
    private Label lblDateDetail;
    private TextArea txtMessageDetail;
    private ListView<Email> emailList;

    /**
     * Construit la vue de la boîte de réception.
     *
     * @param stage        Le stage principal de l'application.
     * @param dbService    Le service pour interagir avec la base de données locale.
     * @param gmailService Le service pour communiquer avec l'API Gmail.
     * @param sessionEmail L'adresse e-mail de l'utilisateur connecté.
     */
    public MailBoxView(Stage stage, DBService dbService, GmailService gmailService, String sessionEmail) {
        this.stage = stage;
        this.dbService = dbService;
        this.gmailService = gmailService;
        this.sessionEmail = sessionEmail;
    }

    /**
     * Affiche la scène principale de la boîte de réception.
     */
    public void show() {
        BorderPane root = new BorderPane();

        // --- Barre latérale (Gauche) ---
        root.setLeft(createSidebar());

        // --- Contenu principal (Centre) ---
        root.setCenter(createCenterContent());

        // --- Logique initiale ---
        setupEventListeners();
        chargerEmails("INBOX", "Boîte de réception");

        stage.setTitle("MailBox - Connecté en tant que " + sessionEmail);
        stage.setScene(new Scene(root, 900, 600));
        stage.centerOnScreen();
    }

    /**
     * Crée et retourne le VBox de la barre latérale.
     *
     * @return Le VBox configuré pour la barre latérale.
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: #f0f0f0; -fx-pref-width: 160px;");

        Label lblUser = new Label("👤 " + sessionEmail);
        lblUser.setStyle("-fx-font-size: 10px; -fx-text-fill: blue;");

        Button btnCompose = createSidebarButton("Nouveau");
        btnCompose.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCompose.setOnAction(e -> afficherFenetreRedaction());

        Button btnInbox = createSidebarButton("Inbox");
        btnInbox.setOnAction(e -> handleInboxRefresh(btnInbox));

        Button btnOutbox = createSidebarButton("Outbox");
        btnOutbox.setOnAction(e -> chargerEmails("OUTBOX", "Boîte d'envoi"));

        Button btnEnsa = createSidebarButton("Dossier ENSA");
        btnEnsa.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        btnEnsa.setOnAction(e -> chargerEmails("ENSA", "Dossier ENSA"));
        
        Button btnDelete = createSidebarButton("Supprimer");
        btnDelete.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> handleDeleteEmail());

        sidebar.getChildren().addAll(lblUser, new Separator(), btnCompose, new Label("Dossiers"), btnInbox, btnOutbox, btnEnsa, new Label("Actions"), btnDelete);
        return sidebar;
    }

    /**
     * Crée un bouton standard pour la barre latérale.
     *
     * @param text Le texte du bouton.
     * @return Un bouton configuré.
     */
    private Button createSidebarButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /**
     * Crée le contenu central de l'application (recherche, liste, détails).
     *
     * @return Le VBox contenant le panneau central.
     */
    private VBox createCenterContent() {
        VBox centerLayout = new VBox(5);
        centerLayout.setPadding(new Insets(10));

        searchField = new TextField();
        searchField.setPromptText("Rechercher un email (Sujet ou Expéditeur)...");

        SplitPane splitPane = new SplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // --- Panneau de la liste (gauche du split) ---
        VBox listPane = new VBox(5);
        titleLabel = new Label("Boîte de réception");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        emailList = new ListView<>(filteredData);
        VBox.setVgrow(emailList, Priority.ALWAYS);
        listPane.getChildren().addAll(titleLabel, emailList);

        // --- Panneau des détails (droite du split) ---
        detailPane = createDetailPane();
        
        splitPane.getItems().addAll(listPane, detailPane);
        splitPane.setDividerPositions(0.4);
        
        centerLayout.getChildren().addAll(searchField, splitPane);
        return centerLayout;
    }

    /**
     * Crée le panneau qui affiche les détails d'un e-mail sélectionné.
     *
     * @return Le VBox configuré pour les détails de l'e-mail.
     */
    private VBox createDetailPane() {
        VBox pane = new VBox(10);
        pane.setPadding(new Insets(15));
        pane.setStyle("-fx-background-color: white;");
        
        lblSujetDetail = new Label("Sélectionnez un email");
        lblSujetDetail.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        lblExpediteurDetail = new Label();
        lblDateDetail = new Label();
        
        txtMessageDetail = new TextArea();
        txtMessageDetail.setEditable(false);
        txtMessageDetail.setWrapText(true);
        VBox.setVgrow(txtMessageDetail, Priority.ALWAYS);
        
        pane.getChildren().addAll(lblSujetDetail, lblExpediteurDetail, lblDateDetail, new Separator(), txtMessageDetail);
        pane.setVisible(false); // Caché par défaut
        return pane;
    }

    /**
     * Configure tous les écouteurs d'événements pour les composants de l'interface.
     */
    private void setupEventListeners() {
        searchField.textProperty().addListener((obs, oldVal, newValue) -> {
            filteredData.setPredicate(email -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                boolean matchSujet = email.getSujet() != null && email.getSujet().toLowerCase().contains(lowerCaseFilter);
                boolean matchExpediteur = email.getExpediteur() != null && email.getExpediteur().toLowerCase().contains(lowerCaseFilter);
                return matchSujet || matchExpediteur;
            });
        });

        emailList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> afficherDetailEmail(newVal));
    }
    
    /**
     * Gère le rafraîchissement de la boîte de réception.
     * <p>
     * Lance un thread pour télécharger les nouveaux e-mails depuis Gmail,
     * les sauvegarde dans la base de données locale s'ils n'existent pas déjà,
     * puis met à jour l'affichage.
     * </p>
     * @param btnInbox Le bouton Inbox pour le désactiver pendant l'opération.
     */
    private void handleInboxRefresh(Button btnInbox) {
        titleLabel.setText("Boîte de réception (Actualisation...)");
        btnInbox.setDisable(true);

        new Thread(() -> {
            List<Email> nouveauxMails = gmailService.recupererInbox();
            int compteurAjouts = 0;
            
            for (Email mail : nouveauxMails) {
                if (!dbService.emailExiste(mail.getExpediteur(), mail.getSujet(), mail.getDate())) {
                    dbService.saveEmail(mail);
                    compteurAjouts++;
                }
            }
            
            final int finalCompteur = compteurAjouts;
            javafx.application.Platform.runLater(() -> {
                chargerEmails("INBOX", "Boîte de réception");
                if (finalCompteur > 0) {
                    System.out.println(finalCompteur + " nouveaux emails synchronisés !");
                }
                btnInbox.setDisable(false);
            });
        }).start();
    }
    
    /**
     * Gère la suppression de l'e-mail sélectionné.
     */
    private void handleDeleteEmail() {
        Email selected = emailList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dbService.deleteEmail(selected.getId());
            masterData.remove(selected);
            detailPane.setVisible(false);
        }
    }

    /**
     * Charge et affiche les e-mails pour un dossier spécifié.
     *
     * @param dossier Le nom du dossier ('INBOX', 'OUTBOX', 'ENSA').
     * @param titre   Le titre à afficher pour ce dossier.
     */
    private void chargerEmails(String dossier, String titre) {
        titleLabel.setText(titre);
        searchField.clear();
        detailPane.setVisible(false);
        masterData.clear();
        masterData.addAll(dbService.getEmails(dossier));
    }

    /**
     * Affiche les détails d'un e-mail spécifique dans le volet de droite.
     *
     * @param email L'e-mail à afficher. Si null, le volet est caché.
     */
    private void afficherDetailEmail(Email email) {
        if (email != null) {
            lblSujetDetail.setText(email.getSujet());
            lblExpediteurDetail.setText("De : " + email.getExpediteur());
            lblDateDetail.setText("Le : " + (email.getDate() != null ? email.getDate().toString() : "Date inconnue"));
            txtMessageDetail.setText(email.getMessage());
            detailPane.setVisible(true);
        } else {
            detailPane.setVisible(false);
        }
    }

    /**
     * Affiche une nouvelle fenêtre modale pour la rédaction d'un e-mail.
     */
    private void afficherFenetreRedaction() {
        Stage stage = new Stage();
        stage.setTitle("Nouveau Message");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        TextField txtDestinataire = new TextField(); txtDestinataire.setPromptText("Destinataire");
        TextField txtSujet = new TextField(); txtSujet.setPromptText("Sujet");
        TextArea txtMessage = new TextArea(); txtMessage.setPromptText("Message...");

        Button btnEnvoyer = new Button("Envoyer");
        btnEnvoyer.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        btnEnvoyer.setOnAction(e -> {
            if (txtDestinataire.getText().isEmpty() || txtSujet.getText().isEmpty()) return;
            
            // Envoyer via Gmail
            gmailService.envoyerEmail(txtDestinataire.getText(), txtSujet.getText(), txtMessage.getText());
            
            // Sauvegarder dans la DB locale (dossier OUTBOX)
            Email mail = new Email(0, sessionEmail, txtDestinataire.getText(), txtSujet.getText(), txtMessage.getText(), null, "OUTBOX");
            dbService.saveEmail(mail);
            
            // Rafraîchir la vue si on est sur la boîte d'envoi
            if (titleLabel.getText().contains("d'envoi")) {
                chargerEmails("OUTBOX", "Boîte d'envoi");
            }
            stage.close();
        });

        layout.getChildren().addAll(new Label("À :"), txtDestinataire, new Label("Sujet :"), txtSujet, new Label("Message :"), txtMessage, btnEnvoyer);
        stage.setScene(new Scene(layout, 400, 450));
        stage.show();
    }
}
