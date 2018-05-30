const Sequelize = require('sequelize');

const { dbConfig } = require('./config');

const sequelize = new Sequelize(dbConfig.database, dbConfig.username, dbConfig.password, {
    host: dbConfig.host,
    dialect: dbConfig.dialect,
    pool: dbConfig.pool,
    operatorsAliases: false
});

const ID_TYPE = Sequelize.INTEGER;

function defineModel(name, attributes, autoId) {
    const attrs = {};
    for (const key in attributes) {
        const value = attributes[key];
        if (typeof value === 'object' && value['type']) {
            value.allowNull = value.allowNull || false;
            attrs[key] = value;
        } else {
            attrs[key] = {
                type: value,
                allowNull: false
            };
        }
    }

    if (autoId === undefined || autoId) {
        attrs.id = {
            type: ID_TYPE,
            primaryKey: true,
            autoIncrement: true
        };
    }

    return sequelize.define(name, attrs, {
        tableName: name,
        timestamps: false
    });
}

const TYPES = ['STRING', 'INTEGER', 'BIGINT', 'TEXT', 'DOUBLE', 'DATEONLY', 'BOOLEAN'];

const db = {
    defineModel: defineModel,
    sync: (args) => {
        sequelize.sync(args);
    }
};

for (const type of TYPES) {
    db[type] = Sequelize[type];
}

module.exports = db;