const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const AppUser = sequelize.define(
  "AppUser",
  {
    id: {
      type: DataTypes.UUID,
      defaultValue: DataTypes.UUIDV4,
      primaryKey: true,
    },
    full_name: { type: DataTypes.TEXT, allowNull: false },
    email: { type: DataTypes.TEXT, allowNull: false, unique: true },
    phone: { type: DataTypes.TEXT },
    password_hash: { type: DataTypes.TEXT, allowNull: false },
    role: { type: DataTypes.ENUM("USER", "PROFESSIONAL"), allowNull: false },
    created_at: { type: DataTypes.DATE, defaultValue: DataTypes.NOW },
  },
  {
    tableName: "app_user",
    timestamps: false,
    indexes: [{ fields: ["email"], unique: true }],
  }
);

module.exports = AppUser;
