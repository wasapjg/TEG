-- src/test/resources/test-data.sql
-- Datos de prueba para tests de integración

-- Insertar continentes de prueba
INSERT INTO continents (id, name, bonus_armies) VALUES
                                                    (1, 'América del Sur', 3),
                                                    (2, 'América del Norte', 5),
                                                    (3, 'África', 3),
                                                    (4, 'Europa', 5),
                                                    (5, 'Asia', 7),
                                                    (6, 'Oceanía', 2);

-- Insertar países de prueba (solo algunos para los tests)
INSERT INTO countries (id, name, continent_id) VALUES
                                                   (1, 'ARGENTINA', 1),
                                                   (2, 'BRASIL', 1),
                                                   (3, 'CHILE', 1),
                                                   (4, 'URUGUAY', 1),
                                                   (5, 'PERU', 1),
                                                   (6, 'COLOMBIA', 1),
                                                   (7, 'MEXICO', 2),
                                                   (8, 'CALIFORNIA', 2),
                                                   (9, 'OREGON', 2),
                                                   (10, 'NUEVA YORK', 2);

-- Insertar fronteras básicas para los tests
INSERT INTO country_neighbors (country_id, neighbor_id) VALUES
                                                            (1, 3), -- ARGENTINA - CHILE
                                                            (1, 5), -- ARGENTINA - PERU
                                                            (1, 4), -- ARGENTINA - URUGUAY
                                                            (1, 2), -- ARGENTINA - BRASIL
                                                            (3, 5), -- CHILE - PERU
                                                            (4, 2), -- URUGUAY - BRASIL
                                                            (5, 6), -- PERU - COLOMBIA
                                                            (5, 2), -- PERU - BRASIL
                                                            (6, 2), -- COLOMBIA - BRASIL
                                                            (6, 7); -- COLOMBIA - MEXICO

-- Insertar usuarios de prueba para los tests de integración
INSERT INTO users (id, username, email, password_hash, is_active, created_at) VALUES
                                                                                  (1, 'testuser1', 'testuser1@test.com', '$2a$10$dummyHashForTestUser1', TRUE, CURRENT_TIMESTAMP),
                                                                                  (2, 'testuser2', 'testuser2@test.com', '$2a$10$dummyHashForTestUser2', TRUE, CURRENT_TIMESTAMP),
                                                                                  (3, 'testuser3', 'testuser3@test.com', '$2a$10$dummyHashForTestUser3', TRUE, CURRENT_TIMESTAMP),
                                                                                  (4, 'testuser4', 'testuser4@test.com', '$2a$10$dummyHashForTestUser4', TRUE, CURRENT_TIMESTAMP);

-- Insertar objetivos de prueba
INSERT INTO objectives (id, type, description, target_data, is_common) VALUES
                                                                           (1, 'COMMON', 'Ocupar 30 países', '{"territoryCount":30}', true),
                                                                           (2, 'OCCUPATION', 'Ocupar América del Sur completo', '{"continents":["América del Sur"]}', false),
                                                                           (3, 'OCCUPATION', 'Ocupar América del Norte completo', '{"continents":["América del Norte"]}', false),
                                                                           (4, 'DESTRUCTION', 'Destruir al ejército rojo', '{"targetColor":"RED"}', false),
                                                                           (5, 'DESTRUCTION', 'Destruir al ejército azul', '{"targetColor":"BLUE"}', false),
                                                                           (6, 'DESTRUCTION', 'Destruir al ejército verde', '{"targetColor":"GREEN"}', false);

-- Insertar perfiles de bots de prueba
INSERT INTO bot_profiles (id, level, strategy, bot_name) VALUES
                                                             (1, 'NOVICE', 'AGGRESSIVE', 'Bot Novato Agresivo Test'),
                                                             (2, 'NOVICE', 'DEFENSIVE', 'Bot Novato Defensivo Test'),
                                                             (3, 'NOVICE', 'BALANCED', 'Bot Novato Equilibrado Test'),
                                                             (4, 'BALANCED', 'AGGRESSIVE', 'Bot Equilibrado Agresivo Test'),
                                                             (5, 'BALANCED', 'DEFENSIVE', 'Bot Equilibrado Defensivo Test'),
                                                             (6, 'BALANCED', 'BALANCED', 'Bot Equilibrado Test'),
                                                             (7, 'EXPERT', 'AGGRESSIVE', 'Bot Experto Agresivo Test');

-- Insertar cartas de prueba
INSERT INTO cards (id, country_id, type, is_in_deck) VALUES
                                                         (1, 1, 'INFANTRY', true),
                                                         (2, 2, 'CAVALRY', true),
                                                         (3, 3, 'CANNON', true),
                                                         (4, 4, 'INFANTRY', true),
                                                         (5, 5, 'CAVALRY', true),
                                                         (6, 6, 'CANNON', true),
                                                         (7, NULL, 'WILDCARD', true),
                                                         (8, NULL, 'WILDCARD', true);