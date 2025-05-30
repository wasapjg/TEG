-- src/main/resources/data.sql
-- Inicialización de datos para TEG

-- Insertar continentes
INSERT INTO continents (name, bonus_armies) VALUES
                                                ('América del Norte', 5),
                                                ('América del Sur', 3),
                                                ('Europa', 5),
                                                ('Asia', 7),
                                                ('África', 3),
                                                ('Oceanía', 2);

-- Insertar países por continente
-- América del Norte
INSERT INTO countries (name, continent_id, position_x, position_y) VALUES
                                                                       ('Alaska', 1, 50, 100),
                                                                       ('Territorio del Noroeste', 1, 120, 120),
                                                                       ('Groenlandia', 1, 280, 80),
                                                                       ('Alberta', 1, 120, 150),
                                                                       ('Ontario', 1, 180, 170),
                                                                       ('Quebec', 1, 220, 160),
                                                                       ('Estados Unidos Occidentales', 1, 120, 200),
                                                                       ('Estados Unidos Orientales', 1, 180, 220),
                                                                       ('América Central', 1, 140, 280);

-- América del Sur
INSERT INTO countries (name, continent_id, position_x, position_y) VALUES
                                                                       ('Venezuela', 2, 200, 320),
                                                                       ('Brasil', 2, 280, 380),
                                                                       ('Perú', 2, 200, 400),
                                                                       ('Argentina', 2, 220, 480);

-- Europa
INSERT INTO countries (name, continent_id, position_x, position_y) VALUES
                                                                       ('Islandia', 3, 320, 120),
                                                                       ('Gran Bretaña', 3, 340, 160),
                                                                       ('Escandinavia', 3, 380, 100),
                                                                       ('Rusia', 3, 420, 140),
                                                                       ('Europa del Norte', 3, 380, 180),
                                                                       ('Europa Occidental', 3, 340, 200),
                                                                       ('Europa del Sur', 3, 380, 220);

-- Asia
INSERT INTO countries (name, continent_id, position_x, position_y) VALUES
                                                                       ('Ural', 4, 460, 140),
                                                                       ('Siberia', 4, 520, 120),
                                                                       ('Yakutsk', 4, 580, 100),
                                                                       ('Kamchatka', 4, 640, 120),
                                                                       ('Irkutsk', 4, 580, 160),
                                                                       ('Mongolia', 4, 580, 200),
                                                                       ('Japón', 4, 660, 220),
                                                                       ('Afganistán', 4, 480, 220),
                                                                       ('China', 4, 560, 240),
                                                                       ('Medio Oriente', 4, 440, 260),
                                                                       ('India', 4, 520, 280),
                                                                       ('Siam', 4, 580, 300);

-- África
INSERT INTO countries (name, continent_id, position_x, position_y) VALUES
                                                                       ('Egipto', 5, 400, 300),
                                                                       ('África del Norte', 5, 360, 320),
                                                                       ('África Oriental', 5, 440, 360),
                                                                       ('Congo', 5, 400, 380),
                                                                       ('África del Sur', 5, 420, 440),
                                                                       ('Madagascar', 5, 480, 460);

-- Oceanía
INSERT INTO countries (name, continent_id, position_x, position_y) VALUES
                                                                       ('Indonesia', 6, 600, 360),
                                                                       ('Nueva Guinea', 6, 660, 380),
                                                                       ('Australia Occidental', 6, 640, 440),
                                                                       ('Australia Oriental', 6, 700, 460);

-- Insertar fronteras (relaciones de vecindad)
-- América del Norte
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES
-- Alaska
(1, 2), (1, 4), (1, 21), -- Alaska conecta con Territorio del Noroeste, Alberta, Kamchatka
-- Territorio del Noroeste
(2, 1), (2, 3), (2, 4), (2, 5), -- conecta con Alaska, Groenlandia, Alberta, Ontario
-- Groenlandia
(3, 2), (3, 5), (3, 6), (3, 12), -- conecta con Territorio del Noroeste, Ontario, Quebec, Islandia
-- Alberta
(4, 1), (4, 2), (4, 5), (4, 7), -- conecta con Alaska, Territorio del Noroeste, Ontario, Estados Unidos Occidentales
-- Ontario
(5, 2), (5, 3), (5, 4), (5, 6), (5, 7), (5, 8), -- conecta con múltiples países
-- Quebec
(6, 3), (6, 5), (6, 8), -- conecta con Groenlandia, Ontario, Estados Unidos Orientales
-- Estados Unidos Occidentales
(7, 4), (7, 5), (7, 8), (7, 9), -- conecta con Alberta, Ontario, Estados Unidos Orientales, América Central
-- Estados Unidos Orientales
(8, 5), (8, 6), (8, 7), (8, 9), -- conecta con Ontario, Quebec, Estados Unidos Occidentales, América Central
-- América Central
(9, 7), (9, 8), (9, 10); -- conecta con Estados Unidos Occidentales, Estados Unidos Orientales, Venezuela

-- América del Sur
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES
-- Venezuela
(10, 9), (10, 11), (10, 12), -- conecta con América Central, Brasil, Perú
-- Brasil
(11, 10), (11, 12), (11, 13), (11, 35), -- conecta con Venezuela, Perú, Argentina, África del Norte
-- Perú
(12, 10), (12, 11), (12, 13), -- conecta con Venezuela, Brasil, Argentina
-- Argentina
(13, 11), (13, 12); -- conecta con Brasil, Perú

-- Europa
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES
-- Islandia
(14, 3), (14, 15), (14, 16), -- conecta con Groenlandia, Gran Bretaña, Escandinavia
-- Gran Bretaña
(15, 14), (15, 16), (15, 17), (15, 19), -- conecta con Islandia, Escandinavia, Rusia, Europa del Norte, Europa Occidental
-- Escandinavia
(16, 14), (16, 15), (16, 17), (16, 19), -- conecta con Islandia, Gran Bretaña, Rusia, Europa del Norte
-- Rusia
(17, 15), (17, 16), (17, 19), (17, 20), (17, 22), (17, 29), -- conecta con múltiples países incluyendo Ural
-- Europa del Norte
(19, 15), (19, 16), (19, 17), (19, 20), (19, 21), -- conecta con varios países europeos
-- Europa Occidental
(20, 19), (20, 21), (20, 35), -- conecta con Europa del Norte, Europa del Sur, África del Norte
-- Europa del Sur
(21, 17), (21, 19), (21, 20), (21, 32), (21, 34), (21, 35); -- conecta con varios países

-- Asia (continuando con las conexiones)
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES
-- Ural
(22, 17), (22, 23), (22, 29), (22, 24), -- conecta con Rusia, Siberia, Afganistán, China
-- Siberia
(23, 22), (23, 24), (23, 25), (23, 26), -- conecta con Ural, Yakutsk, Kamchatka, Irkutsk
-- Yakutsk
(24, 23), (24, 25), (24, 26), -- conecta con Siberia, Kamchatka, Irkutsk
-- Kamchatka
(25, 1), (25, 23), (25, 24), (25, 26), (25, 27), (25, 28), -- conecta con Alaska, Siberia, Yakutsk, Irkutsk, Mongolia, Japón
-- Irkutsk
(26, 23), (26, 24), (26, 25), (26, 27), (26, 28), -- conecta con Siberia, Yakutsk, Kamchatka, Mongolia
-- Mongolia
(27, 25), (27, 26), (27, 28), (27, 30), -- conecta con Kamchatka, Irkutsk, Japón, China
-- Japón
(28, 25), (28, 27), (28, 30), -- conecta con Kamchatka, Mongolia, China
-- Afganistán
(29, 17), (29, 22), (29, 30), (29, 32), (29, 33), -- conecta con Rusia, Ural, China, Medio Oriente, India
-- China
(30, 22), (30, 27), (30, 28), (30, 29), (30, 33), (30, 34), -- conecta con múltiples países
-- Medio Oriente
(32, 21), (32, 29), (32, 33), (32, 36), (32, 37), -- conecta con Europa del Sur, Afganistán, India, Egipto, África Oriental
-- India
(33, 29), (33, 30), (33, 32), (33, 34), -- conecta con Afganistán, China, Medio Oriente, Siam
-- Siam
(34, 30), (34, 33), (34, 43); -- conecta con China, India, Indonesia

-- África
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES
-- Egipto
(36, 21), (36, 32), (36, 35), (36, 37), -- conecta con Europa del Sur, Medio Oriente, África del Norte, África Oriental
-- África del Norte
(35, 11), (35, 20), (35, 21), (35, 36), (35, 38), (35, 39), -- conecta con Brasil, Europa Occidental, Europa del Sur, Egipto, África Oriental, Congo
-- África Oriental
(37, 32), (37, 36), (37, 35), (37, 38), (37, 39), (37, 40), (37, 41), -- conecta con múltiples países africanos
-- Congo
(38, 35), (38, 37), (38, 40), -- conecta con África del Norte, África Oriental, África del Sur
-- África del Sur
(40, 37), (40, 38), (40, 41), -- conecta con África Oriental, Congo, Madagascar
-- Madagascar
(41, 37), (41, 40); -- conecta con África Oriental, África del Sur

-- Oceanía
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES
-- Indonesia
(43, 34), (43, 44), (43, 45), -- conecta con Siam, Nueva Guinea, Australia Occidental
-- Nueva Guinea
(44, 43), (44, 45), (44, 46), -- conecta con Indonesia, Australia Occidental, Australia Oriental
-- Australia Occidental
(45, 43), (45, 44), (45, 46), -- conecta con Indonesia, Nueva Guinea, Australia Oriental
-- Australia Oriental
(46, 44), (46, 45); -- conecta con Nueva Guinea, Australia Occidental

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