const crypto = require('crypto');

const DEFAULT_IV = 'IT_IS_DEFAULT_IV';

const DEFAULT_KEY = 'NOT_HACK_THE_KEY';

module.exports = {
    encrypt: (data, key, iv) => {
        iv = iv || DEFAULT_IV;
        key = key || DEFAULT_KEY;
        const cipher = crypto.createCipheriv('aes-128-cbc', key, iv);
        const encrypted = cipher.update(data, 'utf8', 'base64') + cipher.final('base64');
        return encrypted;
    },
    decrypt: (data, key, iv) => {
        iv = iv || DEFAULT_IV;
        key = key || DEFAULT_KEY;
        const decipher = crypto.createDecipheriv('aes-128-cbc', key, iv);
        const decrypted = decipher.update(data, 'base64', 'utf8') + decipher.final('utf8');
        return decrypted;
    }
}