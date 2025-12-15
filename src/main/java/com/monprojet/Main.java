package com.monprojet;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée principal de l'application MailBox.
 * <p>
 * Cette classe étend {@link Application} de JavaFX et est responsable
 * de l'initialisation de l'application. Son rôle unique est de lancer
 * la première vue, qui est l'écran de connexion (LoginView).
 * </p>
 */
public class Main extends Application {

    /**
     * La méthode principale qui est appelée au lancement de l'application JavaFX.
     *
     * @param primaryStage Le stage (fenêtre) principal fourni par la plateforme JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        // Affiche l'écran de connexion au démarrage
        LoginView loginView = new LoginView(primaryStage);
        loginView.show();
    }

    /**
     * La méthode main standard, utilisée pour lancer l'application.
     *
     * @param args Les arguments de la ligne de commande (non utilisés ici).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
