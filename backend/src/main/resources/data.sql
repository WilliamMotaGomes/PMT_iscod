INSERT INTO users (username, email, password_hash) VALUES
  ('alice',   'alice@example.com',   '$2a$10$yagogTZFx5QRgLkUT2FP1O.JIxxhsTYx8cmETmBA7/JrsjdB9yIFK')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (username, email, password_hash) VALUES
  ('william', 'william@example.com', '$2a$10$yagogTZFx5QRgLkUT2FP1O.JIxxhsTYx8cmETmBA7/JrsjdB9yIFK')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (username, email, password_hash) VALUES
  ('paul',    'paul@example.com',    '$2a$10$yagogTZFx5QRgLkUT2FP1O.JIxxhsTYx8cmETmBA7/JrsjdB9yIFK')
ON CONFLICT (email) DO NOTHING;

INSERT INTO projects (name, description, start_date)
SELECT 'PMT Démo', 'Projet de démonstration', CURRENT_DATE
WHERE NOT EXISTS (
  SELECT 1 FROM projects WHERE name = 'PMT Démo'
);

INSERT INTO project_memberships (project_id, user_id, role)
SELECT p.id, u.id, 'ADMIN'
FROM projects p
JOIN users u ON u.username = 'alice'
WHERE p.name = 'PMT Démo'
  AND NOT EXISTS (
    SELECT 1 FROM project_memberships pm
    WHERE pm.project_id = p.id AND pm.user_id = u.id
  )
ON CONFLICT (project_id, user_id) DO NOTHING;

INSERT INTO project_memberships (project_id, user_id, role)
SELECT p.id, u.id, 'MEMBER'
FROM projects p
JOIN users u ON u.username = 'william'
WHERE p.name = 'PMT Démo'
  AND NOT EXISTS (
    SELECT 1 FROM project_memberships pm
    WHERE pm.project_id = p.id AND pm.user_id = u.id
  )
ON CONFLICT (project_id, user_id) DO NOTHING;

INSERT INTO project_memberships (project_id, user_id, role)
SELECT p.id, u.id, 'OBSERVER'
FROM projects p
JOIN users u ON u.username = 'paul'
WHERE p.name = 'PMT Démo'
  AND NOT EXISTS (
    SELECT 1 FROM project_memberships pm
    WHERE pm.project_id = p.id AND pm.user_id = u.id
  )
ON CONFLICT (project_id, user_id) DO NOTHING;

INSERT INTO tasks (project_id, name, description, due_date, priority, status, assigned_to)
SELECT p.id, 'Configurer CI', 'Configurer GitHub Actions',
       CURRENT_DATE + INTERVAL '5 days', 'HIGH', 'IN_PROGRESS', u.id
FROM projects p
JOIN users u ON u.username = 'william'
WHERE p.name = 'PMT Démo'
  AND NOT EXISTS (
    SELECT 1 FROM tasks t
    WHERE t.project_id = p.id AND t.name = 'Configurer CI'
  );

INSERT INTO tasks (project_id, name, description, due_date, priority, status)
SELECT p.id, 'Dockeriser', 'Dockerfiles back/front',
       CURRENT_DATE + INTERVAL '7 days', 'MEDIUM', 'TODO'
FROM projects p
WHERE p.name = 'PMT Démo'
  AND NOT EXISTS (
    SELECT 1 FROM tasks t
    WHERE t.project_id = p.id AND t.name = 'Dockeriser'
  );
