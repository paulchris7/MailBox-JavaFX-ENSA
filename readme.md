# MailBox - JavaFX Email Client

A robust desktop application for real-time email management. This project combines a modern user interface (**JavaFX**) with local data persistence (**MySQL**) and secure network communication (**Gmail API**).

## Key Features

* **Secure Authentication:** Secure login via App Password to Google servers.
* **Send & Receive:** Full support for SMTP (Sending) and IMAP (Synchronized Receiving).
* **Smart Local Storage:** Emails are fetched and saved in a local MySQL database for offline access.
* **SQL Automation (Triggers):**
    * *Auto-Archiving:* Deleted emails are intercepted by a Trigger and saved in an `archives` table before removal.
    * *Smart Sorting:* Emails related to "ENSA" are automatically detected and redirected to a specific folder via SQL logic.
* **Rich UI:** Real-time search bar, SplitPane reading view, and responsive design.

## Tech Stack

* **Language:** Java 17+
* **GUI:** JavaFX (Modular architecture)
* **Database:** MySQL / MariaDB (via XAMPP)
* **Network:** JavaMail API (javax.mail)
* **Build Tool:** Maven

## Setup & Installation

### 1. Prerequisites
* JDK 17 or higher.
* Maven.
* MySQL Server (e.g., XAMPP).

### 2. Database Configuration
Import the provided `setup_database.sql` script into your SQL manager (phpMyAdmin) to:
1.  Create the `mailbox_db` database.
2.  Generate the necessary tables and **SQL Triggers**.

### 3. Run the Application
Navigate to the project root and run:
```bash
mvn clean javafx:run