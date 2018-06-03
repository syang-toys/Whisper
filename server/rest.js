const crypto = require('./crypto');
const str = require('./utils').str;

module.exports = {
    kv: undefined,
    APIError: class {
        constructor(code, message) {
            this.code = code || 'internal:unknown_error';
            this.message = message || '';
        }
    },
    restify: (pathPrefix) => {
        pathPrefix = pathPrefix || '/api/';
        return async (ctx, next) => {
            if (ctx.request.path.startsWith(pathPrefix)) {
                ctx.rest = (data) => {
                    ctx.response.type = 'application/json';
                    ctx.response.body = data;
                }
                try {
                    await next();
                } catch (e) {
                    console.log(e);
                    console.log(`Process API error: ${e.code}`);
                    ctx.response.status = 400;
                    ctx.response.type = 'application/json';
                    ctx.response.body = {
                        code: e.code || 'internal:unknown_error',
                        message: e.message || ''
                    };
                }
            } else {
                await next();
            }
        };
    },
    decrypt: () => {
        return async (ctx, next) => {
            const kv = crypto.decrypt('rsa', ctx.request.body.key);
            this.kv = kv;
            ctx.request.body = JSON.parse(crypto.decrypt('xxtea', ctx.request.body.data, kv));
            await next();
        }
    },
    encrypt: () => {
        return async (ctx, next) => {
            const kkv = Array.from(this.kv).map((c)=>{
                return c.charCodeAt().toString(36);
            }).join('');

            const sessionKey = kkv.substr(0, 8) + str.getRandomString(8);
            const sessionIV = kkv.substr(8, 8) + str.getRandomString(8);

            const data = crypto.encrypt('aes', JSON.stringify(ctx.response.body), sessionKey, sessionIV);
            const key = crypto.encrypt('xxtea', sessionKey, this.kv);
            const iv = crypto.encrypt('xxtea', sessionIV, this.kv);

            ctx.response.body = {
                data,
                key,
                iv
            };

            await next();
        }
    }
};
