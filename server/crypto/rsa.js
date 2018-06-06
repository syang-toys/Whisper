const fs = require('fs');
const NodeRSA = require('node-rsa');

const serverKey = new NodeRSA(fs.readFileSync(__dirname + '/../assets/privatekey.pem', 'utf8'));
serverKey.setOptions({ encryptionScheme: 'pkcs1' });

module.exports = {
    encrypt: (data, key) => {
        rsaKey = key || serverKey;
        return rsaKey.encrypt(data, 'base64');
    },
    decrypt: (data, key) => {
        rsaKey = key || serverKey;        
        return rsaKey.decrypt(data, 'utf8');
    }
}