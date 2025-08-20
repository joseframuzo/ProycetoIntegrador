// app.js
const express = require("express");
const cors = require("cors");
const morgan = require("morgan");
const bodyParser = require("body-parser");

const { sequelize } = require("./models"); // importa sequelize desde models/index.js

// Rutas
const authRoutes = require("./routes/auth.routes");
const professionalsRoutes = require("./routes/professionals.routes");
const publicRoutes = require("./routes/public.routes");
const conversationsRoutes = require("./routes/conversations.routes");

const app = express();

// Middlewares
app.use(cors());
app.use(morgan("dev"));
app.use(bodyParser.json({ limit: "2mb" })); // pediste body-parser

// Mount de rutas
app.use("/auth", authRoutes);
app.use("/professionals", professionalsRoutes);
app.use("/public", publicRoutes);
app.use("/conversations", conversationsRoutes);

// 404 por defecto
app.use((req, res) => res.status(404).json({ error: "Not found" }));

// Arranque
const PORT = process.env.PORT || 3000;

(async () => {
  try {
    await sequelize.authenticate();
    await sequelize.sync(); // { alter: true } si estás ajustando columnas

    app.listen(PORT, "0.0.0.0", () => {
      console.log(`API lista en http://localhost:${PORT}`);
    });
  } catch (err) {
    console.error("Error iniciando:", err);
    process.exit(1);
  }
})();
