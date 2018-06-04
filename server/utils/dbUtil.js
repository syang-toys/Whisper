const User = require('../models/user');
const Friend = require('../models/friend');

module.exports = {
    isFriend: async (id1, id2) => {
        return await Friend.findOne({
            attributes: ['valid'],
            where: {
                $or: [{
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
        const friends = await Friend.findAll({ where: { $or: [{ id1: id }, { id2: id }], valid: true } });
        return friends.map((friend)=>{
            return friend.id1 === id ? friend.id2 : friend.id1
        }).map(async (friendId)=>{
            return await getUserById(friendId, ['id', 'email', 'publicKey']);
        });
    }
}