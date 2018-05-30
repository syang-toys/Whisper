const xxtea = require('xxtea-node');

module.exports = {
    encrypt: function (data, key) {
        return xxtea.encryptToString(data, key);
    },
    decrypt: function (data, key) {
        return xxtea.decryptToString(data, key);
    }
}