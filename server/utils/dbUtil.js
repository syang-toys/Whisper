const User = require('../models/user');
const Friend = require('../models/friend');

module.exports = {
    isFriend: async (id1, id2) => {
        return await Friend.findOne({
            attributes: ['valid'],
            where: {
                id1,
                id2
            }
        });
    },
    createFriend: async (id1, id2) => {
        await Friend.create({id1, id2, valid: true});
    },
    getUser: async (email, attributes) => {
        const query = {
            where: {
                email
            }
        }
        if(attributes) {
            query.attributes = attributes;
        }
        return await User.findOne(query);
    },
    createUser: async (user) => {
        await User.create(user);
    }
}