const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const ProfessionalPhoto = sequelize.define(
  "ProfessionalPhoto",
  {
    id: {
      type: DataTypes.UUID,
      defaultValue: DataTypes.UUIDV4,
      primaryKey: true,
    },
    professional_id: { type: DataTypes.UUID, allowNull: false },
    url: { type: DataTypes.TEXT, allowNull: false },
    created_at: { type: DataTypes.DATE, defaultValue: DataTypes.NOW },
  },
  {
    tableName: "professional_photo",
    timestamps: false,
    indexes: [{ fields: ["professional_id"] }],
  }
);

module.exports = ProfessionalPhoto;
