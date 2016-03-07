-- Project Phase 2: Relational Model
-- Thomas Liu
-- Cassandra Garner

DROP TABLE MUser CASCADE;
DROP TABLE User_list CASCADE;
DROP TABLE Message CASCADE;
DROP TABLE Chat CASCADE;
DROP TABLE Chat_list CASCADE;
DROP TABLE User_list_contains CASCADE;

CREATE TABLE User_list(
	list_type CHAR(10) NOT NULL,
	id INT NOT NULL PRIMARY KEY UNIQUE);

CREATE TABLE MUser(
	phoneNum CHAR(13) UNIQUE NOT NULL,
	login CHAR(50) UNIQUE PRIMARY KEY,
	password CHAR(50) NOT NULL,
	status CHAR(140),
	block_list INT NOT NULL UNIQUE,
	contact_list INT NOT NULL UNIQUE,
	FOREIGN KEY(block_list) REFERENCES User_list(id),
	FOREIGN KEY(contact_list) REFERENCES User_list(id));

CREATE TABLE Chat(
	id INT PRIMARY KEY,
	chat_type CHAR(10) NOT NULL,
	initial_sender CHAR(50) NOT NULL,
	FOREIGN KEY(initial_sender) REFERENCES MUser(login));

CREATE TABLE Message(
	id INT PRIMARY KEY,
	text CHAR(140) NOT NULL,
	timestamp TIMESTAMP NOT NULL,
	status CHAR(30) NOT NULL,
	sender CHAR(50) NOT NULL,
	contains INT NOT NULL,
	FOREIGN KEY(contains) REFERENCES Chat(id),
	FOREIGN KEY(sender) REFERENCES MUser(login));

CREATE TABLE Chat_list(
	login CHAR(50) NOT NULL,
	chat INT NOT NULL,
	FOREIGN KEY(login) REFERENCES MUser,
	FOREIGN KEY(chat) REFERENCES Chat(id),
	PRIMARY KEY(login, chat));

CREATE TABLE User_list_contains(
	login CHAR(50) NOT NULL,
	user_list INT NOT NULL,
	FOREIGN KEY(login) REFERENCES MUser,
	FOREIGN KEY(user_list) REFERENCES User_list(id),
	PRIMARY KEY(login, user_list));

