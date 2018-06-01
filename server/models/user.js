const db = require('../db');

module.exports = db.defineModel('users', {
    email: {
        type: db.STRING(64),
        unique: true
    },
    passwd: db.STRING(64),
    publicKey: db.TEXT,
    privateKey: db.TEXT
});