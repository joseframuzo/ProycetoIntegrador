const { DataTypes } = require("sequelize");
const sequelize = require("../config/database");

const ConversationParticipant = sequelize.define(
  "ConversationParticipant",
  {
    conversation_id: { type: DataTypes.UUID, primaryKey: true },
    user_id: { type: DataTypes.UUID, primaryKey: true },
  },
  {
    tableName: "conversation_participant",
    timestamps: false,
    indexes: [{ fields: ["user_id"] }],
  }
);

module.exports = ConversationParticipant;
