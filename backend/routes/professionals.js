const express = require('express');
const multer = require('multer');
const path = require('path');
const db = require('../db');
const auth = require('../middleware/auth');

const router = express.Router();

// Listar categorías
router.get('/categories', async (req, res) => {
  const r = await db.query('SELECT slug, name FROM service_category ORDER BY name');
  res.json(r.rows);
});

// Listar profesionales (opcional ?category=slug)
router.get('/', async (req, res) => {
  const { category } = req.query;
  const params = [];
  let sql = 'SELECT * FROM v_professional_card';
  if (category) { sql += ' WHERE main_category=$1'; params.push(category); }
  const r = await db.query(sql, params);
  res.json(r.rows);
});

// Detalle profesional
router.get('/:id', async (req, res) => {
  const { id } = req.params;
  const prof = await db.query('SELECT * FROM v_professional_card WHERE professional_id=$1', [id]);
  if (!prof.rows.length) return res.status(404).json({ error: 'No encontrado' });
  const photos = await db.query('SELECT id, url FROM professional_photo WHERE professional_id=$1 ORDER BY created_at', [id]);
  res.json({ ...prof.rows[0], photos: photos.rows });
});

// Subida de fotos (solo PROFESSIONAL dueño)
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, path.join(__dirname, '..', 'uploads')),
  filename: (req, file, cb) => cb(null, Date.now() + '-' + file.originalname)
});
const upload = multer({ storage });

router.post('/:id/photos', auth, upload.single('photo'), async (req, res) => {
  try {
    const { id } = req.params;
    // verificar que el id pertenece al usuario autenticado
    const own = await db.query(
      `SELECT p.id FROM professional_profile p WHERE p.id=$1 AND p.user_id=$2`,
      [id, req.user.id]
    );
    if (!own.rows.length) return res.status(403).json({ error: 'No autorizado' });
    const url = `/uploads/${req.file.filename}`;
    await db.query('INSERT INTO professional_photo(professional_id,url) VALUES ($1,$2)', [id, url]);
    res.json({ ok: true, url });
  } catch (e) {
    console.error(e); res.status(500).json({ error: 'Error subiendo foto' });
  }
});

module.exports = router;