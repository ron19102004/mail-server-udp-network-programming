## MAIL SERVER - CLIENT WITH UDP 
***
### Step run code
#### 1: Clone source
```bash
git clone https://github.com/ron19102004/mail-server-udp-network-programming.git
```
#### 2: Run server in path
```xpath
server/src/main/java/com/ronial/app/MailServerApplication.java
```
#### 3: Copy sql be generated (to create table in database) in terminal or log frame such as
```text
[23:32:12 - 24/03/2025] MySQLConnector ➜ Create table query: 
CREATE TABLE IF NOT EXISTS users(id INT PRIMARY KEY AUTO_INCREMENT,fullName VARCHAR(255) NOT NULL,email VARCHAR(255) NOT NULL UNIQUE,password TEXT NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS emails(id INT PRIMARY KEY AUTO_INCREMENT,sender INT ,recipient INT ,subject TEXT NOT NULL,body LONGTEXT NOT NULL,links LONGTEXT ,transferFrom VARCHAR(255) ,is_seen BOOL NOT NULL,is_sender_remove BOOL NOT NULL,is_recipient_remove BOOL NOT NULL,created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,FOREIGN KEY (sender) REFERENCES users(id) ON DELETE CASCADE,FOREIGN KEY (recipient) REFERENCES users(id) ON DELETE CASCADE)
```
#### 4: Copy database conf properties be generated to create database in terminal or log frame such as
```text
[23:32:12 - 24/03/2025] DatabaseConf ➜ 
		DatabaseProperty:
		hostname = localhost
		port = 3306
		username = root
		password = 
		database = mail_server_udp
```
***
### Notes
- Project be used [JDK23](https://www.oracle.com/java/technologies/javase/jdk23-archive-downloads.html)
- Database be used [MySQL](https://www.mysql.com/)