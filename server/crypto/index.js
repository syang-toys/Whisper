const fs = require('fs');

const cryptoAlgorithmMap = new Map();

function crypto(name, type, data, key, iv) {
    const algorithm = cryptoAlgorithmMap.get(name);
    if (algorithm === undefined) {
        return data;
    }
    const func = algorithm[type];
    if (func === undefined) {
        return data;
    }
    return func(data, key, iv);
}

fs.readdirSync(__dirname).filter(f => {
    return f !== 'index.js' && f.endsWith('.js');
}).forEach(f => {
    const name = f.substring(0, f.length - 3);
    cryptoAlgorithmMap.set(name, require(`${__dirname}/${f}`));
});

module.exports = {
    encrypt: function (name, data, key, iv) {
        return crypto(name, 'encrypt', data, key, iv);
    },
    decrypt: function (name, data, key, iv) {
        return crypto(name, 'decrypt', data, key, iv);
    }
}