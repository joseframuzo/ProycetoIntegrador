const router = require("express").Router();
const {
  ProfessionalProfile,
  ProfessionalPhoto,
  AppUser,
} = require("../models");
const { Op } = require("sequelize");

// GET /public/professionals
router.get("/professionals", async (req, res) => {
  try {
    const { city, category, q, limit = 20, offset = 0 } = req.query;
    const where = {};
    if (city) where.city = city;
    if (category) where.main_category = category;

    const include = [{ model: AppUser, attributes: ["full_name"] }];
    const list = await ProfessionalProfile.findAll({
      where,
      include,
      limit: Number(limit),
      offset: Number(offset),
      order: [
        ["verified", "DESC"],
        ["created_at", "DESC"],
      ],
    });

    // filtrar por q en full_name o headline
    const filtered = q
      ? list.filter(
          (p) =>
            (p.headline || "")
              .toLowerCase()
              .includes(String(q).toLowerCase()) ||
            (p.AppUser?.full_name || "")
              .toLowerCase()
              .includes(String(q).toLowerCase())
        )
      : list;

    res.json(
      filtered.map((p) => ({
        professional_id: p.id,
        full_name: p.AppUser?.full_name,
        main_category: p.main_category,
        headline: p.headline,
        city: p.city,
        verified: p.verified,
      }))
    );
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error listando" });
  }
});

// GET /public/professionals/:id
router.get("/professionals/:id", async (req, res) => {
  try {
    const p = await ProfessionalProfile.findByPk(req.params.id, {
      include: [
        { model: AppUser, attributes: ["full_name", "email", "phone"] },
        { model: ProfessionalPhoto, attributes: ["id", "url", "created_at"] },
      ],
    });
    if (!p) return res.status(404).json({ error: "No encontrado" });
    res.json(p);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error detalle" });
  }
});

module.exports = router;
