const db = require('../db');

module.exports = db.defineModel('users', {
    email: {
        type: db.STRING,
        unique: true
    },
    passwd: db.STRING(32),
    publicKey: db.TEXT,
    privateKey: db.TEXT
});