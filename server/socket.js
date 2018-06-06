const NodeRSA = require('node-rsa');
const crypto = require('./crypto');
const { db, str } = require('./utils');

const userMap = new Map();
const userSocketMap = new Map();

const serverDecrypt = (key, data) => {
    const xxteaKey = crypto.decrypt('rsa', key);
    return crypto.decrypt('xxtea', data, xxteaKey);
};

const serverEncrypt = (clientId, data) => {
    const xxteaKey = str.getRandomString(32);
    const secret = crypto.encrypt('xxtea', data, xxteaKey);
    const key = crypto.encrypt('rsa', xxteaKey, userMap.get(clientId).publicKey);
    return [key, secret];
}

module.exports = io => {
    io.on('connection', socket => {

        socket.on('online', async (key, data) => {
            
            const email = serverDecrypt(key, data);

            const user = await db.getUser(email, ['id', 'publicKey']);
            if (!user) {
                return;
            }

            const rsaPublicKey = new NodeRSA(user.publicKey);
            rsaPublicKey.setOptions({ encryptionScheme: 'pkcs1' });
            
            userMap.set(socket.id, { email: email, id: user.id, publicKey: rsaPublicKey });
            userSocketMap.set(user.id, socket.id);

            const friends = await db.getFriends(user.id);

            const secret = serverEncrypt(socket.id, JSON.stringify(friends));

            io.to(socket.id).emit('friends', secret[0], secret[1]);
        });

        socket.on('offline', async () => {

            const id = userMap.get(socket.id).id;

            userSocketMap.delete(id);

            userMap.delete(socket.id);
        });

        socket.on('friend request', async (key, data, ack) => {

            const friendEmail = serverDecrypt(key, data);

            const friend = await db.getUser(friendEmail, ['id', 'publicKey']);

            if (!friend) {
                ack(...serverEncrypt(socket.id, 'no such user!'));
                return;
            }

            const self = userMap.get(socket.id);

            const valid = await db.isFriend(self.id, friend.id);

            if (valid) {
                ack(...serverEncrypt(socket.id, 'already being friends!'));
                return;
            }

            const friendClientId = userSocketMap.get(friend.id);

            if (friendClientId !== undefined) {
                const secret = serverEncrypt(friendClientId, self.email);
                io.to(friendClientId).emit('new friend request', secret[0], secret[1]);
            }

            ack(...serverEncrypt(socket.id, 'send friend request successfully!'));
        });

        socket.on('accept friend request', async (key, data, ack) => {
            const friendEmail = serverDecrypt(key, data);

            const friend = await db.getUser(friendEmail, ['id', 'publicKey']);

            const self = userMap.get(socket.id);

            await db.createFriend(self.id, friend.id);

            const user = {id: friend.id, email: friendEmail, publicKey: friend.publicKey};
            
            ack(...serverEncrypt(socket.id, JSON.stringify(user)));

            const friendClientId = userSocketMap.get(friend.id);

            if (friendClientId !== undefined) {
                const secret = serverEncrypt(friendClientId, JSON.stringify(self));
                io.to(friendClientId).emit('friend request accepted', secret[0], secret[1]);
            }
        });

        socket.on('deny friend request', async friendEmail => {
            return;
        });

        socket.on('initial chatting', (key, data, ack) => {
            const plain = serverDecrypt(key, data);
            const {id, key1} = JSON.parse(plain);

            const friendClientId = userSocketMap.get(parseInt(id));

            if (friendClientId === undefined) {
                ack(...serverEncrypt(socket.id, 'friend not online!'));
            } else {
                const user = userMap.get(socket.id);
                const forward = {id: user.id, key1: key1};
                const secret = serverEncrypt(friendClientId, JSON.stringify(forward));
                io.to(friendClientId).emit('receive initial chatting', secret[0], secret[1]);
            }
        });

        socket.on('reply initial chatting', (key, data, ack) => {
            const plain = serverDecrypt(key, data);
            const {id, key2} = JSON.parse(plain);

            const friendClientId = userSocketMap.get(parseInt(id));
            
            if (friendClientId === undefined) {
                ack(...serverEncrypt(socket.id, 'friend not online!'));
            } else {
                const user = userMap.get(socket.id);
                const forward = {id: user.id, key2: key2};
                const secret = serverEncrypt(friendClientId, JSON.stringify(forward));
                io.to(friendClientId).emit('initial chatting reply', secret[0], secret[1]);
                ack(...serverEncrypt(socket.id, 'exchange secret key!'));
            }
        });

        socket.on('text msg', (key, id, encryptedContent, encryptedSignature) => {
            const friendId = parseInt(serverDecrypt(key, id));

            const friendClientId = userSocketMap.get(friendId);

            const self = userMap.get(socket.id);

            const secret = serverEncrypt(friendClientId, `${self.id}`);

            io.to(friendClientId).emit('text msg received', secret[0], secret[1], encryptedContent, encryptedSignature);
        });

        socket.on('file msg', (key, id, encryptedFileName, encryptedContent, encryptedSignature) => {
            const friendId = parseInt(serverDecrypt(key, id));

            console.log(friendId);
            console.log(encryptedFileName);

            const friendClientId = userSocketMap.get(friendId);

            const self = userMap.get(socket.id);

            const secret = serverEncrypt(friendClientId, `${self.id}`);

            io.to(friendClientId).emit('file msg received', secret[0], secret[1], encryptedFileName, encryptedContent, encryptedSignature);
        });
    });
};
