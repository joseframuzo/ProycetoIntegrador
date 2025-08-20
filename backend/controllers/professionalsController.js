const {
  AppUser,
  ProfessionalProfile,
  ProfessionalPhoto,
  ServiceCategory,
} = require("../models");

exports.saveNotes = async (req, res) => {
  try {
    const {
      user_id,
      notes,
      experience_years,
      headline,
      about,
      city,
      main_category, // <-- AHORA LEÍMOS main_category
    } = req.body;

    if (!user_id) return res.status(400).json({ error: "user_id requerido" });

    // Validar main_category (opcional pero recomendado)
    let mcToSet;
    if (typeof main_category === "string" && main_category.trim()) {
      const slug = main_category.trim().toLowerCase();
      // Verifica que exista en el catálogo
      const cat = await ServiceCategory.findByPk(slug);
      if (!cat) {
        return res.status(400).json({ error: "main_category inválida" });
      }
      mcToSet = slug; // slug válido ('arquitecto','plomero','pintor','albanil')
    }

    let prof = await ProfessionalProfile.findOne({ where: { user_id } });

    if (!prof) {
      // Si no existe perfil, crear uno (si no mandan main_category, usa cualquiera del catálogo o un default)
      let defaultCat = (await ServiceCategory.findOne())?.slug || "pintor";
      prof = await ProfessionalProfile.create({
        user_id,
        cedula: `PEND-${Date.now()}`,
        main_category: mcToSet ?? defaultCat,
        headline,
        about,
        city,
        experience_years,
        notes,
      });
    } else {
      // Actualiza solo lo enviado
      const patch = {};
      if (notes !== undefined) patch.notes = notes;
      if (experience_years !== undefined)
        patch.experience_years = experience_years;
      if (headline !== undefined) patch.headline = headline;
      if (about !== undefined) patch.about = about;
      if (city !== undefined) patch.city = city;
      if (mcToSet !== undefined) patch.main_category = mcToSet; // <-- ACTUALIZA LA CATEGORÍA

      if (Object.keys(patch).length > 0) {
        await prof.update(patch);
      }
    }

    // Responder el perfil actualizado
    res.json(prof);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error guardando notas" });
  }
};

exports.addPhotoUrl = async (req, res) => {
  try {
    const { user_id, url } = req.body;
    if (!user_id || !url)
      return res.status(400).json({ error: "user_id y url requeridos" });
    const prof = await ProfessionalProfile.findOne({ where: { user_id } });
    if (!prof)
      return res.status(403).json({ error: "No hay perfil profesional" });
    const photo = await ProfessionalPhoto.create({
      professional_id: prof.id,
      url,
    });
    res.status(201).json(photo);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error agregando foto" });
  }
};

exports.deletePhoto = async (req, res) => {
  try {
    const { photo_id, user_id } = req.body;
    if (!photo_id || !user_id)
      return res.status(400).json({ error: "photo_id y user_id requeridos" });
    const photo = await ProfessionalPhoto.findByPk(photo_id);
    if (!photo) return res.status(404).json({ error: "No existe" });

    const prof = await ProfessionalProfile.findOne({
      where: { id: photo.professional_id, user_id },
    });
    if (!prof) return res.status(403).json({ error: "No autorizado" });

    await photo.destroy();
    res.json({ deleted: true });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error eliminando foto" });
  }
};
