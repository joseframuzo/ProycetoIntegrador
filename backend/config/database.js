const { Sequelize } = require("sequelize");

const sequelize = new Sequelize(
  process.env.DB_NAME || "servicios",
  process.env.DB_USER || "postgres",
  process.env.DB_PASS || "1234",
  {
    host: process.env.DB_HOST || "db",   // importante: "db" es el servicio de postgres
    port: Number(process.env.DB_PORT || 5432),
    dialect: process.env.DB_DIALECT || "postgres",
    logging: process.env.LOG_SQL === "true" ? (msg) => console.log("[SQL]", msg) : false,
  }
);

module.exports = sequelize;
