const User = require('../models/user');
const Friend = require('../models/friend');

const Op =  require('sequelize').Op;

module.exports = {
    isFriend: async (id1, id2) => {
        return await Friend.findOne({
            attributes: ['valid'],
            where: {
                [Op.or]: [{
                    id1: id1,
                    id2: id2
                }, {
                    id1: id2,
                    id2: id1
                }]
            }
        });
    },
    createFriend: async (id1, id2) => {
        await Friend.create({
            id1,
            id2,
            valid: true
        });
    },
    getUser: async (email, attributes) => {
        const query = {
            where: {
                email
            }
        }
        if (attributes) {
            query.attributes = attributes;
        }
        return await User.findOne(query);
    },
    getUserById: async (id, attributes) => {
        const query = {
            where: {
                id
            }
        }
        if (attributes) {
            query.attributes = attributes;
        }
        return await User.findOne(query);
    },
    createUser: async (user) => {
        await User.create(user);
    },
    getFriends: async (id) => {
        const relationships = await Friend.findAll({ where: { [Op.or]: [{id1: id}, {id2: id}], valid: true } });
        const ids = relationships.map((relationship)=>{
            return relationship.id1 === id ? relationship.id2 : relationship.id1
        });
        const friends = [];
        for(let friendId of ids) {
            const friend = await await User.findOne({where: {id: friendId}, attributes: ['id', 'email', 'publicKey']});
            friends.push(friend);
        }
        return friends.map((friend)=>{
            return {id: friend.id, email: friend.email, publicKey: friend.publicKey};
        });        
    }
}