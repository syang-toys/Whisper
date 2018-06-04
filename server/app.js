const Koa = require('koa');

const bodyParser = require('koa-bodyparser');

const controller = require('./controller');

const { restify, decrypt, encrypt } = require('./rest');

const app = new Koa();

app.use(bodyParser());

app.use(restify());

app.use(decrypt());

app.use(controller());

app.use(encrypt());

const server = app.listen(3000);

const io = require('socket.io').listen(server);

socket(io);

console.log('app listen port: 3000');