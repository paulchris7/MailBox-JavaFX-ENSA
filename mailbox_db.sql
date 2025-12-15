DROP DATABASE IF EXISTS mailbox_db;
CREATE DATABASE mailbox_db;
USE mailbox_db;

-- 1. Table des emails
CREATE TABLE emails (
    id INT AUTO_INCREMENT PRIMARY KEY,
    expediteur VARCHAR(100) NOT NULL,
    destinataire VARCHAR(100) NOT NULL,
    sujet VARCHAR(255),
    message TEXT,
    date_envoi DATETIME DEFAULT CURRENT_TIMESTAMP,
    dossier VARCHAR(20) DEFAULT 'INBOX' 
);

-- 2. Table d'archives
CREATE TABLE archives (
    id_archive INT AUTO_INCREMENT PRIMARY KEY,
    id_original INT,
    sujet VARCHAR(255),
    date_suppression DATETIME DEFAULT CURRENT_TIMESTAMP,
    raison VARCHAR(50) DEFAULT 'Suppression utilisateur'
);

-- 3. Trigger 1 : Archivage avant suppression
CREATE TRIGGER avant_suppression_email
BEFORE DELETE ON emails
FOR EACH ROW
INSERT INTO archives (id_original, sujet)
VALUES (OLD.id, OLD.sujet);

-- 4. Trigger 2 : Tri automatique ENSA (Version simplifiée pour phpMyAdmin)
CREATE TRIGGER tri_automatique_ensa
BEFORE INSERT ON emails
FOR EACH ROW
SET NEW.dossier = IF(NEW.sujet LIKE '%ensa%' OR NEW.expediteur LIKE '%ensa%', 'ENSA', NEW.dossier);

-- 5. Données de test
INSERT INTO emails (expediteur, destinataire, sujet, message, dossier, date_envoi) VALUES 
('directeur@ensa.ma', 'paul@ensa.ma', 'Confirmation inscription', 'Bienvenue en 4ème année.', 'INBOX', '2025-12-10 09:00:00'),
('amazon@service.com', 'paul@gmail.com', 'Votre colis', 'Livraison prévue demain.', 'INBOX', '2025-12-14 14:30:00'),
('paul@gmail.com', 'prof@ensa.ma', 'Rendu du Projet', 'Voici mon code source.', 'OUTBOX', '2025-12-14 23:00:00');