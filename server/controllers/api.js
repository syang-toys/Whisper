const bcrypt = require('bcrypt');
const NodeRSA = require('node-rsa');
const DB = require('../utils').db;
const crypto = require('../crypto');
const APIError = require('../rest').APIError;

module.exports = {
    'POST /api/register': async (ctx, next) => {

        const { email, password } = ctx.request.body;

        if (!email || !password) {
            throw new APIError('register:wrong arguments!');        
        }
        if (await DB.getUser(email, ['id'])) {
            throw new APIError('register:user already exists!');
        }

        const passwd = await bcrypt.hash(password, 10);
        const key = new NodeRSA({ b: 2048 });
        const privateKey = crypto.encrypt('xxtea', key.exportKey('private'), passwd);
        const publicKey = key.exportKey('public');

        const user = {
            email,
            passwd,
            privateKey,
            publicKey
        }

        await DB.createUser(user);

        ctx.rest({});

        await next();
    },

    'POST /api/login': async (ctx, next) => {
        const { email, password } = ctx.request.body;
         if (!email || !password) {
            throw new APIError('login:wrong arguments!');        
        }
        const user = await DB.getUser(email, ['id', 'passwd', 'privateKey']);
        if (!user) {
            throw new APIError('login:user doesn\'t exist!');
        }
        if(!await bcrypt.compare(password, user.passwd)) {
            throw new APIError('login:wrong password');            
        }
        
        ctx.rest({
            id: user.id,
            privateKey: user.privateKey
        });

        await next();
    },

    'GET /api/user': async (ctx, next) => {
        const { email } = ctx.request.body;

        if (!email) {
            throw new APIError('user:you need a email');        
        }

        const user = await db.getUser(email, ['id', 'publicKey']);        
        
        if (!user) {
            throw new APIError('user:user doesn\'t exists!');
        }

        ctx.rest({
            id: user.id,
            publicKey: user.publicKey
        });

        await next();
    }
};