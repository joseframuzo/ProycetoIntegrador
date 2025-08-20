const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const ServiceCategory = sequelize.define(
  "ServiceCategory",
  {
    slug: { type: DataTypes.TEXT, primaryKey: true },
    name: { type: DataTypes.TEXT, allowNull: false },
  },
  {
    tableName: "service_category",
    timestamps: false,
  }
);

module.exports = ServiceCategory;
