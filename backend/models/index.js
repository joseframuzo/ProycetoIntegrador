const sequelize = require("../config/database");

// Importa modelos ya definidos (no los invoques)
const ServiceCategory = require("./ServiceCategory");
const AppUser = require("./AppUser");
const ProfessionalProfile = require("./ProfessionalProfile");
const ProfessionalPhoto = require("./ProfessionalPhoto");
const Conversation = require("./Conversation");
const ConversationParticipant = require("./ConversationParticipant");
const Message = require("./Message");

/* =========================
   Asociaciones
   ========================= */

// AppUser ↔ ProfessionalProfile (1:1)
ProfessionalProfile.belongsTo(AppUser, {
  foreignKey: "user_id",
  onDelete: "CASCADE",
});
AppUser.hasOne(ProfessionalProfile, {
  foreignKey: "user_id",
});

// ProfessionalProfile ↔ ServiceCategory (N:1 por slug)
ProfessionalProfile.belongsTo(ServiceCategory, {
  foreignKey: "main_category",
  targetKey: "slug",
});

// ProfessionalProfile ↔ ProfessionalPhoto (1:N)
ProfessionalPhoto.belongsTo(ProfessionalProfile, {
  foreignKey: "professional_id",
  onDelete: "CASCADE",
});
ProfessionalProfile.hasMany(ProfessionalPhoto, {
  foreignKey: "professional_id",
});

// Conversation ↔ ConversationParticipant (1:N)
ConversationParticipant.belongsTo(Conversation, {
  foreignKey: "conversation_id",
  onDelete: "CASCADE",
});
Conversation.hasMany(ConversationParticipant, {
  foreignKey: "conversation_id",
});

// AppUser ↔ ConversationParticipant (1:N)
ConversationParticipant.belongsTo(AppUser, {
  foreignKey: "user_id",
  onDelete: "CASCADE",
});
AppUser.hasMany(ConversationParticipant, {
  foreignKey: "user_id",
});

// Conversation ↔ Message (1:N)
Message.belongsTo(Conversation, {
  foreignKey: "conversation_id",
  onDelete: "CASCADE",
});
Conversation.hasMany(Message, {
  foreignKey: "conversation_id",
});

// AppUser (sender) ↔ Message (1:N)
Message.belongsTo(AppUser, {
  as: "sender",
  foreignKey: "sender_id",
  onDelete: "CASCADE",
});
AppUser.hasMany(Message, {
  as: "sentMessages",
  foreignKey: "sender_id",
});

module.exports = {
  sequelize,
  ServiceCategory,
  AppUser,
  ProfessionalProfile,
  ProfessionalPhoto,
  Conversation,
  ConversationParticipant,
  Message,
};
