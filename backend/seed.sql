
 
CREATE EXTENSION IF NOT EXISTS pgcrypto; 
 

CREATE TABLE IF NOT EXISTS service_category (
  slug TEXT PRIMARY KEY,          -
  name TEXT NOT NULL
);
 
INSERT INTO service_category (slug, name) VALUES
  ('arquitecto','Arquitecto'),
  ('plomero','Plomero'),
  ('pintor','Pintor'),
  ('albanil','Albañil')
ON CONFLICT (slug) DO NOTHING;
 

CREATE TABLE IF NOT EXISTS app_user (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  full_name TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  phone TEXT,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('USER','PROFESSIONAL')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
 
CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email);
 

CREATE TABLE IF NOT EXISTS professional_profile (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE REFERENCES app_user(id) ON DELETE CASCADE,
  cedula TEXT UNIQUE NOT NULL, --
  main_category TEXT NOT NULL REFERENCES service_category(slug),
  headline TEXT,               
  about TEXT,
  city TEXT,
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  experience_years INTEGER,    
  notes TEXT,                  
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
 
CREATE INDEX IF NOT EXISTS idx_prof_profile_city ON professional_profile (city);
CREATE INDEX IF NOT EXISTS idx_prof_profile_category ON professional_profile (main_category);
 

CREATE TABLE IF NOT EXISTS professional_photo (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  professional_id UUID NOT NULL REFERENCES professional_profile(id) ON DELETE CASCADE,
  url TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
 
CREATE INDEX IF NOT EXISTS idx_prof_photo_professional ON professional_photo (professional_id);
 

CREATE OR REPLACE VIEW v_professional_card AS
SELECT p.id AS professional_id,
       u.full_name,
       p.main_category,
       p.headline,
       COALESCE(
         (SELECT url
            FROM professional_photo ph
           WHERE ph.professional_id = p.id
           ORDER BY ph.created_at ASC
           LIMIT 1),
         ''
       ) AS photo_url,
       p.city,
       p.verified
FROM professional_profile p
JOIN app_user u ON u.id = p.user_id;
 

CREATE TABLE IF NOT EXISTS conversation (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
 
CREATE TABLE IF NOT EXISTS conversation_participant (
  conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  PRIMARY KEY (conversation_id, user_id)
);
 
CREATE INDEX IF NOT EXISTS idx_conv_part_user ON conversation_participant (user_id);
 
CREATE TABLE IF NOT EXISTS message (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
  sender_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  body TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
 
CREATE INDEX IF NOT EXISTS idx_message_conv ON message (conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_message_sender ON message (sender_id);
 

INSERT INTO service_category (slug, name) VALUES
  ('electricista','Electricista'),
  ('jardinero','Jardinero')
ON CONFLICT (slug) DO NOTHING;
 

WITH ins AS (
  INSERT INTO app_user (full_name, email, phone, password_hash, role)
  VALUES
    ('Juan Pérez', 'juan@example.com', '099111222', 'hash123', 'USER'),
    ('María López', 'maria@example.com', '098333444', 'hash456', 'USER'),
    ('Carlos Gómez', 'carlos@pro.com', '097555666', 'hash789', 'PROFESSIONAL'),
    ('Ana Torres', 'ana@pro.com', '096777888', 'hash321', 'PROFESSIONAL')
  ON CONFLICT (email) DO NOTHING
  RETURNING id, email
)
SELECT * FROM ins;
 

WITH profs AS (
  SELECT id, email FROM app_user WHERE email IN ('carlos@pro.com','ana@pro.com')
)
INSERT INTO professional_profile (user_id, cedula, main_category, headline, about, city, verified, experience_years, notes)
VALUES
  ((SELECT id FROM profs WHERE email = 'carlos@pro.com'),
   '1717706210', 'plomero', 'Plomero con experiencia en reparaciones',
   'Más de 10 años solucionando fugas y reparaciones de cañerías.', 'Quito', TRUE, 10, 'Atención rápida'),
  ((SELECT id FROM profs WHERE email = 'ana@pro.com'),
   '1718805321', 'pintor', 'Pintora decorativa y artística',
   'Especialista en pintura decorativa y remodelación de interiores.', 'Guayaquil', FALSE, 5, 'Trabajo personalizado')
ON CONFLICT (cedula) DO NOTHING;
 

INSERT INTO professional_photo (professional_id, url)
VALUES
  ((SELECT id FROM professional_profile WHERE cedula = '1717706210'), 'https://picsum.photos/200/300?random=1'),
  ((SELECT id FROM professional_profile WHERE cedula = '1718805321'), 'https://picsum.photos/200/300?random=2')
ON CONFLICT DO NOTHING;
 
WITH conv AS (
  INSERT INTO conversation DEFAULT VALUES RETURNING id
),
users AS (
  SELECT id, email FROM app_user WHERE email IN ('juan@example.com','carlos@pro.com')
)
INSERT INTO conversation_participant (conversation_id, user_id)
SELECT conv.id, users.id
FROM conv, users;
 

WITH conv AS (
  SELECT id FROM conversation ORDER BY created_at DESC LIMIT 1
),
juan AS (SELECT id FROM app_user WHERE email = 'juan@example.com'),
carlos AS (SELECT id FROM app_user WHERE email = 'carlos@pro.com')
INSERT INTO message (conversation_id, sender_id, body)
VALUES
  ((SELECT id FROM conv), (SELECT id FROM juan), 'Hola, necesito un plomero urgente'),
  ((SELECT id FROM conv), (SELECT id FROM carlos), 'Hola Juan, puedo pasar en la tarde a revisarlo');
 
