-- src/main/resources/data.sql
-- Inicialización de datos para TEG
use teg;
-- Insertar continentes
INSERT INTO continents (name, bonus_armies) VALUES
                                                ('América del Sur', 3),
                                                ('América del Norte', 5),
                                                ('África', 3),
                                                ('Europa', 5),
                                                ('Asia', 7),
                                                ('Oceanía', 2);

-- Insertar países por continente
-- América del Sur (ID_CONTINENTE = 1)
INSERT INTO countries (name, continent_id) VALUES ('ARGENTINA', 1);
INSERT INTO countries (name, continent_id) VALUES ('BRASIL', 1);
INSERT INTO countries (name, continent_id) VALUES ('CHILE', 1);
INSERT INTO countries (name, continent_id) VALUES ('URUGUAY', 1);
INSERT INTO countries (name, continent_id) VALUES ('PERU', 1);
INSERT INTO countries (name, continent_id) VALUES ('COLOMBIA', 1);

-- América del Norte (ID_CONTINENTE = 2)
INSERT INTO countries (name, continent_id) VALUES ('MEXICO', 2);
INSERT INTO countries (name, continent_id) VALUES ('CALIFORNIA', 2);
INSERT INTO countries (name, continent_id) VALUES ('OREGON', 2);
INSERT INTO countries (name, continent_id) VALUES ('NUEVA YORK', 2);
INSERT INTO countries (name, continent_id) VALUES ('TERRANOVA', 2);
INSERT INTO countries (name, continent_id) VALUES ('LABRADOR', 2);
INSERT INTO countries (name, continent_id) VALUES ('GROENLANDIA', 2);
INSERT INTO countries (name, continent_id) VALUES ('CANADA', 2);
INSERT INTO countries (name, continent_id) VALUES ('ALASKA', 2);
INSERT INTO countries (name, continent_id) VALUES ('YUKOM', 2);



-- África (ID_CONTINENTE = 3)
INSERT INTO countries (name, continent_id) VALUES ('SAHARA', 3);
INSERT INTO countries (name, continent_id) VALUES ('ETIOPIA', 3);
INSERT INTO countries (name, continent_id) VALUES ('EGIPTO', 3);
INSERT INTO countries (name, continent_id) VALUES ('ZAIRE', 3);
INSERT INTO countries (name, continent_id) VALUES ('SUDAFRICA', 3);
INSERT INTO countries (name, continent_id) VALUES ('MADAGASCAR', 3);

-- Europa (ID_CONTINENTE = 4)
INSERT INTO countries (name, continent_id) VALUES ('ESPAÑA', 4);
INSERT INTO countries (name, continent_id) VALUES ('ISLANDIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('FRANCIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('ITALIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('ALEMANIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('POLONIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('RUSIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('SUECIA', 4);
INSERT INTO countries (name, continent_id) VALUES ('GRAN BRETAÑA', 4);


-- Asia (ID_CONTINENTE = 5)
INSERT INTO countries (name, continent_id) VALUES ('TURQUIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('ISRAEL', 5);
INSERT INTO countries (name, continent_id) VALUES ('ARABIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('IRAN', 5);
INSERT INTO countries (name, continent_id) VALUES ('INDIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('GOBI', 5);
INSERT INTO countries (name, continent_id) VALUES ('MONGOLIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('CHINA', 5);
INSERT INTO countries (name, continent_id) VALUES ('JAPON', 5);
INSERT INTO countries (name, continent_id) VALUES ('SIBERIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('ARAL', 5);
INSERT INTO countries (name, continent_id) VALUES ('TARTARIA', 5);
INSERT INTO countries (name, continent_id) VALUES ('TAYMIR', 5);
INSERT INTO countries (name, continent_id) VALUES ('KAMCHATKA', 5);
INSERT INTO countries (name, continent_id) VALUES ('MALASIA', 5);

-- Oceanía (ID_CONTINENTE = 6)
INSERT INTO countries (name, continent_id) VALUES ('JAVA', 6);
INSERT INTO countries (name, continent_id) VALUES ('AUSTRALIA', 6);
INSERT INTO countries (name, continent_id) VALUES ('SUMATRA', 6);
INSERT INTO countries (name, continent_id) VALUES ('BORNEO', 6);

-- Insertar fronteras (relaciones de vecindad
-- ARGENTINA (ID 1)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 3); -- CHILE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 5); -- PERU
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 4); -- URUGUAY
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (1, 2); -- BRASIL

-- CHILE (ID 3)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (3, 32); -- AUSTRALIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (3, 5);  -- PERU

-- URUGUAY (ID 4)
-- Ya fue agregada la relación con ARGENTINA y BRASIL
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (4, 2); -- BRASIL

-- PERU (ID 5)
-- Ya fueron agregadas: ARGENTINA, CHILE
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (5, 6); -- COLOMBIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (5, 2); -- BRASIL

-- COLOMBIA (ID 6)
-- Ya fue agregada: PERU
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (6, 11); -- MEXICO
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (6, 2);  -- BRASIL

-- BRASIL (ID 2)
-- Ya fueron agregadas: URUGUAY, COLOMBIA, PERU, ARGENTINA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (2, 17); -- SAHARA


-- MEXICO (11)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (11, 12); -- CALIFORNIA

-- CALIFORNIA (12)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (12, 13); -- OREGON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (12, 14); -- NUEVA YORK

-- OREGON (13)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (13, 14); -- NUEVA YORK
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (13, 19); -- ALASKA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (13, 20); -- YUKON
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (13, 18); -- CANADA

-- ALASKA (19)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 28); -- KAMCHATKA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 20); -- YUKON

-- YUKON (20)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (20, 18); -- CANADA

-- CANADA (18)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 14); -- NUEVA YORK
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 15); -- TERRANOVA

-- NUEVA YORK (14)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (14, 17); -- GROENLANDIA
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (14, 15); -- TERRANOVA

-- TERRANOVA (15)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (15, 16); -- LABRADOR

-- GROENLANDIA (17)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 16); -- LABRADOR
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 22); -- ISLANDIA
-- Europa
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (24, 31);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (24, 13);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (24, 30);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (30, 29);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (31, 23);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (31, 27);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (23, 25);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (23, 17);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (25, 27);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (25, 26);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (26, 27);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (27, 28);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (28, 29);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (28, 19);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (28, 32);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 42);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 35);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (29, 32);

-- África
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 2);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 19);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 18);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (17, 20);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 18);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 22);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 32);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (19, 33);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 20);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (18, 21);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (20, 21);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (20, 22);

-- Asia
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (43, 42);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (43, 44);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 35);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 41);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (42, 38);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (44, 41);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 43);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 45);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 38);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (41, 39);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (45, 39);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (45, 40);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (45, 15);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (40, 39);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 39);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 37);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (38, 35);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 37);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 35);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 36);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (39, 46);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (37, 35);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 32);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (35, 36);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 33);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (32, 34);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (33, 34);

-- Oceanía
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (36, 49);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (46, 36);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (46, 50);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (49, 48);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (50, 48);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (47, 48);
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES (48, 3);

-- Insertar objetivos secretos
INSERT INTO objectives (type, description, target_data, is_common) VALUES
                                                                       ('OCCUPATION', 'Ocupar África, 5 países de América del Norte y 4 países de Europa', '{"continents":["África"],"regions":[{"continent":"América del Norte","count":5},{"continent":"Europa","count":4}]}', false),
                                                                       ('OCCUPATION', 'Ocupar América del Sur, 7 países de Europa y 3 países limítrofes entre sí en cualquier lugar del mapa', '{"continents":["América del Sur"],"regions":[{"continent":"Europa","count":7}],"adjacent":3}', false),
                                                                       ('OCCUPATION', 'Ocupar Asia y 2 países de América del Sur', '{"continents":["Asia"],"regions":[{"continent":"América del Sur","count":2}]}', false),
                                                                       ('OCCUPATION', 'Ocupar Europa, 4 países de Asia y 2 países de América del Sur', '{"continents":["Europa"],"regions":[{"continent":"Asia","count":4},{"continent":"América del Sur","count":2}]}', false),
                                                                       ('OCCUPATION', 'Ocupar América del Norte, 2 países de Oceanía y 4 de Asia', '{"continents":["América del Norte"],"regions":[{"continent":"Oceanía","count":2},{"continent":"Asia","count":4}]}', false),
                                                                       ('OCCUPATION', 'Ocupar 2 países de Oceanía, 2 países de África, 2 países de América del Sur, 3 países de Europa, 4 de América del Norte y 3 de Asia', '{"regions":[{"continent":"Oceanía","count":2},{"continent":"África","count":2},{"continent":"América del Sur","count":2},{"continent":"Europa","count":3},{"continent":"América del Norte","count":4},{"continent":"Asia","count":3}]}', false),
                                                                       ('OCCUPATION', 'Ocupar Oceanía, América del Norte y 2 países de Europa', '{"continents":["Oceanía","América del Norte"],"regions":[{"continent":"Europa","count":2}]}', false),
                                                                       ('OCCUPATION', 'Ocupar América del Sur, África y 4 países de Asia', '{"continents":["América del Sur","África"],"regions":[{"continent":"Asia","count":4}]}', false),
                                                                       ('OCCUPATION', 'Ocupar Oceanía, África y 5 países de América del Norte', '{"continents":["Oceanía","África"],"regions":[{"continent":"América del Norte","count":5}]}', false),
                                                                       ('DESTRUCTION', 'Destruir el ejército azul, de ser imposible al jugador de la derecha', '{"targetColor":"BLUE"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército rojo, de ser imposible al jugador de la derecha', '{"targetColor":"RED"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército negro, de ser imposible al jugador de la derecha', '{"targetColor":"BLACK"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército amarillo, de ser imposible al jugador de la derecha', '{"targetColor":"YELLOW"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército verde, de ser imposible al jugador de la derecha', '{"targetColor":"GREEN"}', false),
                                                                       ('DESTRUCTION', 'Destruir al ejército magenta, de ser imposible al jugador de la derecha', '{"targetColor":"PURPLE"}', false);

-- Insertar objetivo común
INSERT INTO objectives (type, description, target_data, is_common) VALUES
    ('COMMON', 'Ocupar 30 países', '{"territoryCount":30}', true);

-- Insertar cartas de países
INSERT INTO cards (country_id, type, is_in_deck)
SELECT id,
       CASE
           WHEN (id % 3) = 1 THEN 'INFANTRY'
           WHEN (id % 3) = 2 THEN 'CAVALRY'
           ELSE 'CANNON'
           END,
       true
FROM countries;

-- Insertar cartas wildcard
INSERT INTO cards (country_id, type, is_in_deck) VALUES
                                                     (NULL, 'WILDCARD', true),
                                                     (NULL, 'WILDCARD', true);

-- Insertar perfiles de bots
INSERT INTO bot_profiles (level, strategy, bot_name) VALUES
                                                         ('NOVICE', 'AGGRESSIVE', 'Bot Novato Agresivo'),
                                                         ('NOVICE', 'DEFENSIVE', 'Bot Novato Defensivo'),
                                                         ('NOVICE', 'BALANCED', 'Bot Novato Equilibrado'),
                                                         ('BALANCED', 'AGGRESSIVE', 'Bot Equilibrado Agresivo'),
                                                         ('BALANCED', 'DEFENSIVE', 'Bot Equilibrado Defensivo'),
                                                         ('BALANCED', 'BALANCED', 'Bot Equilibrado'),
                                                         ('BALANCED', 'OBJECTIVE_FOCUSED', 'Bot Enfocado en Objetivos'),
                                                         ('EXPERT', 'AGGRESSIVE', 'Bot Experto Agresivo'),
                                                         ('EXPERT', 'DEFENSIVE', 'Bot Experto Defensivo'),
                                                         ('EXPERT', 'BALANCED', 'Bot Experto Equilibrado'),
                                                         ('EXPERT', 'OBJECTIVE_FOCUSED', 'Bot Experto Estratega');

-- insertar usuarios para probar endopoints
INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (1, 'usuario1', 'usuario1@example.com', 'Hash123#', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (2, 'usuario2', 'usuario2@example.com', 'Hash234#', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (3, 'usuario3', 'usuario3@example.com', 'Hash345#', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (4, 'usuario4', 'usuario4@example.com', 'Hash456#', TRUE, CURRENT_TIMESTAMP);
INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (5, 'usuario5', 'usuario5@example.com', 'Hash567#', TRUE, CURRENT_TIMESTAMP);

INSERT INTO USERS (ID, USERNAME, EMAIL, PASSWORD_HASH, IS_ACTIVE, CREATED_AT)
VALUES (6, 'usuario6', 'usuario6@example.com', 'Hash678#', TRUE, CURRENT_TIMESTAMP);

