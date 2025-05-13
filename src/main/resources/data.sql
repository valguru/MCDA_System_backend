DELETE FROM rating;
DELETE FROM alternative;
DELETE FROM criteria;
DELETE FROM question;
DELETE FROM expert_team;
DELETE FROM team;
DELETE FROM expert;

-- ========== ENUM TYPES ==========
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'scale_type') THEN
        CREATE TYPE scale_type AS ENUM ('SHORT', 'BASE', 'LONG', 'NUMERIC');
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'optimization_direction') THEN
        CREATE TYPE optimization_direction AS ENUM ('MIN', 'MAX');
    END IF;
END $$;

-- ========== USERS / EXPERTS ==========
INSERT INTO expert (email, password, name) VALUES
('alice@example.com', '123', 'Alice'),
('bob@example.com', '456', 'Bob'),
('carol@example.com', '789', 'Carol');

-- ========== TEAMS ==========
INSERT INTO team (name, created_by) VALUES
('Agile Ninjas', 1),
('Scrum Masters', 2);

-- ========== TEAM MEMBERSHIPS ==========
INSERT INTO expert_team (expert_id, team_id) VALUES
(1, 1),
(2, 1),
(3, 1),
(2, 2),
(3, 2);

-- ========== QUESTIONS ==========
INSERT INTO question (title, description, team_id, created_by) VALUES
('Выбор системы контроля версий', 'Нужно выбрать оптимальный вариант для проекта', 1, 1),
('Выбор таск-трекера', 'Сравниваем Jira, Trello и Notion', 2, 2);

-- ========== CRITERIA ==========
INSERT INTO criteria (name, scale, optimization_direction, question_id) VALUES
('Удобство использования', 'BASE', 'MAX', 1),
('Интеграция с CI/CD', 'BASE', 'MAX', 1),
('Кривая обучения', 'BASE', 'MIN', 1);

-- ========== ALTERNATIVES ==========
INSERT INTO alternative (title, description, question_id) VALUES
('GitHub', 'Популярная платформа с богатым функционалом', 1),
('GitLab', 'Интеграция с CI/CD, open-source', 1),
('Bitbucket', 'Интеграция с Jira', 1);

-- ========== RATINGS (от Alice для вопроса 1) ==========
INSERT INTO rating (expert_id, alternative_id, criteria_id, question_id, value) VALUES
(1, 1, 1, 1, 5),
(1, 1, 2, 1, 4),
(1, 1, 3, 1, 3),

(1, 2, 1, 1, 4),
(1, 2, 2, 1, 5),
(1, 2, 3, 1, 2),

(1, 3, 1, 1, 3),
(1, 3, 2, 1, 3),
(1, 3, 3, 1, 4);

---- ========== TEAM INVITATIONS ==========
--CREATE TABLE IF NOT EXISTS team_invitation (
--    id SERIAL PRIMARY KEY,
--    team_id BIGINT NOT NULL REFERENCES team(id),
--    expert_id BIGINT NOT NULL REFERENCES expert(id),
--    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED')),
--    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--    UNIQUE (team_id, expert_id)
--);
--
---- Пример приглашения
--INSERT INTO team_invitation (team_id, expert_id, status)
--VALUES (2, 1, 'PENDING'); -- Alice приглашена в Scrum Masters
