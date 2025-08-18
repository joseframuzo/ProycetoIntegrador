const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const db = require('../db');
const { isCedulaEcuador } = require('../utils/cedula');

const router = express.Router();

// Helper para firmar token
function signUser(u) {
  return jwt.sign(
    { id: u.id, role: u.role },
    process.env.JWT_SECRET || 'devsecret',
    { expiresIn: '7d' }
  );
}

/* =========================
   POST /api/auth/register
   ========================= */
router.post(
  '/register',
  body('full_name').notEmpty(),
  body('email').isEmail(),
  body('password').isLength({ min: 6 }),
  body('role').isIn(['USER', 'PROFESSIONAL']),
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) return res.status(400).json({ errors: errors.array() });

    try {
      let { full_name, email, phone, password, role } = req.body;
      email = String(email).trim().toLowerCase();
      role = (role || 'USER').toUpperCase();

      // ¿email ya existe?
      const dup = await db.query(
        'SELECT 1 FROM public.app_user WHERE LOWER(email) = $1',
        [email]
      );
      if (dup.rowCount) return res.status(409).json({ error: 'Email ya registrado' });

      const hash = await bcrypt.hash(password, 10);

      // inserta usuario (id UUID por defecto en BD)
      const ins = await db.query(
        `INSERT INTO public.app_user (full_name, email, phone, password_hash, role)
         VALUES ($1,$2,$3,$4,$5)
         RETURNING id, full_name, email, role`,
        [full_name, email, phone || null, hash, role]
      );
      const user = ins.rows[0];

      // si es profesional, crea perfil
      if (role === 'PROFESSIONAL') {
        const { cedula, main_category, headline, about, city } = req.body;
        if (!cedula || !isCedulaEcuador(cedula))
          return res.status(400).json({ error: 'Cédula inválida' });
        if (!main_category)
          return res.status(400).json({ error: 'main_category requerido' });

        await db.query(
          `INSERT INTO public.professional_profile
             (user_id, cedula, main_category, headline, about, city, verified)
           VALUES ($1,$2,$3,$4,$5,$6,false)`,
          [user.id, cedula, main_category, headline || null, about || null, city || null]
        );
      }

      const token = signUser(user);
      return res.status(201).json({ token, user });
    } catch (e) {
      if (e.code === '23505')
        return res.status(400).json({ error: 'Email o cédula ya registrados' });
      console.error('[REGISTER] error', e);
      return res.status(500).json({ error: 'Error del servidor' });
    }
  }
);

/* ======================
   POST /api/auth/login
   ====================== */
router.post('/login', async (req, res) => {
  try {
    const email = String(req.body.email || '').trim().toLowerCase();
    const password = String(req.body.password || '').trim();
    if (!email || !password)
      return res.status(400).json({ error: 'Email y password requeridos' });

    // Tabla correcta
    const q = await db.query(
      `SELECT id, full_name, email, password_hash, role
         FROM public.app_user
        WHERE LOWER(email) = $1`,
      [email]
    );

    if (q.rowCount === 0)
      return res.status(401).json({ error: 'Credenciales incorrectas' });

    const u = q.rows[0];
    const ok = await bcrypt.compare(password, u.password_hash || '');
    if (!ok)
      return res.status(401).json({ error: 'Credenciales incorrectas' });

    const token = jwt.sign(
      { id: u.id, role: u.role },
      process.env.JWT_SECRET || 'devsecret',
      { expiresIn: '7d' }
    );

    return res.json({
      token,
      user: { id: u.id, full_name: u.full_name, email: u.email, role: u.role }
    });
  } catch (e) {
    console.error('[LOGIN] error', e);
    return res.status(500).json({ error: 'Error en login' });
  }
});


module.exports = router;
