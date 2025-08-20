const router = require("express").Router();
const { Conversation, ConversationParticipant, Message } = require("../models");
const { Op } = require("sequelize");

// POST /conversations/open
router.post("/open", async (req, res) => {
  try {
    const { from_user_id, to_user_id } = req.body;
    if (!from_user_id || !to_user_id)
      return res
        .status(400)
        .json({ error: "from_user_id y to_user_id requeridos" });

    // buscar conv con ambos participantes
    const convs = await Conversation.findAll({
      include: [
        {
          model: ConversationParticipant,
          where: { user_id: { [Op.in]: [from_user_id, to_user_id] } },
        },
      ],
    });

    let conv = null;
    for (const c of convs) {
      const parts = await ConversationParticipant.count({
        where: { conversation_id: c.id },
      });
      const both = await ConversationParticipant.count({
        where: {
          conversation_id: c.id,
          user_id: { [Op.in]: [from_user_id, to_user_id] },
        },
      });
      if (parts === 2 && both === 2) {
        conv = c;
        break;
      }
    }

    if (!conv) {
      conv = await Conversation.create({});
      await ConversationParticipant.bulkCreate([
        { conversation_id: conv.id, user_id: from_user_id },
        { conversation_id: conv.id, user_id: to_user_id },
      ]);
    }
    res.status(201).json({ conversation_id: conv.id });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error abriendo conversación" });
  }
});

// POST /conversations/send
router.post("/send", async (req, res) => {
  try {
    const { conversation_id, sender_id, body } = req.body;
    if (!conversation_id || !sender_id || !body)
      return res.status(400).json({ error: "Faltan campos" });
    const msg = await Message.create({ conversation_id, sender_id, body });
    res.status(201).json(msg);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error enviando" });
  }
});

// GET /conversations/:id/messages
router.get("/:id/messages", async (req, res) => {
  try {
    const { limit = 50 } = req.query;
    const msgs = await Message.findAll({
      where: { conversation_id: req.params.id },
      order: [["created_at", "ASC"]],
      limit: Number(limit),
    });
    res.json(msgs);
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Error listando mensajes" });
  }
});

module.exports = router;
