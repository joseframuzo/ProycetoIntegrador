const router = require("express").Router();
const ctrl = require("../controllers/professionalsController");
router.post("/me/notes", ctrl.saveNotes);
router.post("/me/photos", ctrl.addPhotoUrl);
router.delete("/me/photos", ctrl.deletePhoto);
module.exports = router;
