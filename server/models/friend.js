const db = require('../db');

module.exports = db.defineModel('friends', {
    user1: db.INTEGER,
    user2: db.INTEGER,
    valid: db.BOOLEAN
});