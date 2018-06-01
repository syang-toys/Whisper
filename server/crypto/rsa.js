const fs = require('fs');
const NodeRSA = require('node-rsa');

const key = new NodeRSA(fs.readFileSync(__dirname + '/../assets/privatekey.pem', 'utf8'));
key.setOptions({ encryptionScheme: 'pkcs1' });

module.exports = {
    encrypt: (data) => {
        return key.encrypt(data, 'base64');
    },
    decrypt: (data) => {
        return key.decrypt(data, 'utf8');
    }
}