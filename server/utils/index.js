module.exports = {
    db: require('./dbUtil'),
    str: {
        getRandomString: function (n) {
            const str = [];
            n = n || 8;
            while (n > 0) {
                const length = Math.min(n, 8);
                Math.random().toString(36).substr(2, length);
                n -= 8;
            }
            return str.join('');
        }
    }
}