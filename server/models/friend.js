const db = require('../db');

module.exports = db.defineModel('friends', {
    id1: db.INTEGER,
    id2: db.INTEGER,
    valid: db.BOOLEAN
}, false);