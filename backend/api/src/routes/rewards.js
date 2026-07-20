const express = require('express');
const router = express.Router();
const rewardController = require('../controllers/rewardController');
const auth = require('../middlewares/auth');

router.use(auth);

router.get('/', rewardController.getRewards);
router.post('/redeem', rewardController.redeemReward);

module.exports = router;
