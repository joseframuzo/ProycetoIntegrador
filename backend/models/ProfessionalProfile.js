const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const ProfessionalProfile = sequelize.define(
  "ProfessionalProfile",
  {
    id: {
      type: DataTypes.UUID,
      defaultValue: DataTypes.UUIDV4,
      primaryKey: true,
    },
    user_id: { type: DataTypes.UUID, allowNull: false, unique: true },
    cedula: { type: DataTypes.TEXT, allowNull: false, unique: true },
    main_category: { type: DataTypes.TEXT, allowNull: false }, // FK a service_category.slug
    headline: DataTypes.TEXT,
    about: DataTypes.TEXT,
    city: DataTypes.TEXT,
    verified: { type: DataTypes.BOOLEAN, defaultValue: false },
    experience_years: DataTypes.INTEGER,
    notes: DataTypes.TEXT,
    created_at: { type: DataTypes.DATE, defaultValue: DataTypes.NOW },
  },
  {
    tableName: "professional_profile",
    timestamps: false,
  }
);

module.exports = ProfessionalProfile;
