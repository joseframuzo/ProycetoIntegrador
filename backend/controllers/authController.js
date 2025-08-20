const bcrypt = require("bcrypt");
const { AppUser } = require("../models");

exports.register = async (req, res) => {
  try {
    const { full_name, email, phone, password, role } = req.body;
    if (!full_name || !email || !password || !role) {
      return res
        .status(400)
        .json({ error: "full_name, email, password, role requeridos" });
    }
    const exists = await AppUser.findOne({ where: { email } });
    if (exists) return res.status(409).json({ error: "Email ya registrado" });

    const password_hash = await bcrypt.hash(password, 10);
    const user = await AppUser.create({
      full_name,
      email,
      phone,
      password_hash,
      role,
    });
    res.status(201).json(user);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error registrando" });
  }
};

exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;
    const user = await AppUser.findOne({ where: { email } });
    if (!user) return res.status(401).json({ error: "Credenciales inválidas" });
    const ok = await bcrypt.compare(password, user.password_hash);
    if (!ok) return res.status(401).json({ error: "Credenciales inválidas" });
    res.json({
      id: user.id,
      full_name: user.full_name,
      email: user.email,
      phone: user.phone,
      role: user.role,
      created_at: user.created_at,
    });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error en login" });
  }
};
