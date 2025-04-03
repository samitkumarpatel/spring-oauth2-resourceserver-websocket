CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE
);

INSERT INTO users (id, username) VALUES (1, 'Bret');
INSERT INTO users (id, username) VALUES (2, 'Antonette');
INSERT INTO users (id, username) VALUES (3, 'Samantha');
INSERT INTO users (id, username) VALUES (4, 'Karianne');
INSERT INTO users (id, username) VALUES (5, 'Kamren');
INSERT INTO users (id, username) VALUES (6, 'Leopoldo_Corkery');
INSERT INTO users (id, username) VALUES (7, 'Elwyn.Skiles');
INSERT INTO users (id, username) VALUES (8, 'Maxime_Nienow');
INSERT INTO users (id, username) VALUES (9, 'Delphine');
INSERT INTO users (id, username) VALUES (10, 'Moriah.Stanton');


CREATE TABLE messages (
      id SERIAL PRIMARY KEY,
      sender_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      receiver_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      content TEXT NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      is_read BOOLEAN DEFAULT false
);
