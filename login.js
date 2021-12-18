const jwt = require("jsonwebtoken")

exports.usersJSON = {
    "users": {
        "user1": {
            "login": "u1",
            "password": "p1"
        },
        "user2": {
            "login": "u2",
            "password": "p2"
        }
    },
    "logins": [
        "u1", "u2"
    ],
}

exports.post_auth = (req, res) => {
    try {
        const {login, password} = req.body;
        if (login == null || password == null) {
            return res.status(400).json({"error": `missing login or password`})
        }
        let users = JSON.parse(JSON.stringify(exports.usersJSON))

        if (users.logins.indexOf(login) !== -1) {
            return res.status(403).json({message: `User with login ${login} already exists`})
        }

        res.json({
            login: login
        })
        exports.usersJSON.users[login] = {
            "login": login,
            "password": password
        }
        exports.usersJSON.logins.push(login)
    } catch (e) {
        console.log(e)
        res.send({message: "Server error"})
    }
}


exports.post_login = (req, res) => {
    try {
        const {login, password} = req.body;
        if (login == null || password == null) {
            return res.status(400).json({"error": `missing login or password`})
        }
        let users = JSON.parse(JSON.stringify(exports.usersJSON))

        if (users.logins.indexOf(login) === -1) {
            return res.status(403).json({message: `User with login ${login} doesn't exist`})
        }

        let user = users.users[Object.keys(users.users).find(user => users.users[user].login === login)]

        if (user.password !== password) {
            return res.status(403).json({message: "Invalid password"})
        }

        const token = jwt.sign({login: user.login, password: user.password}, "secretKey", {expiresIn: "1h"})

        res.json({
            token,
            user: {
                login: user.login,
                access_level: 1
            }
        })
    } catch (err) {
        res.status(500).json({
            message:
                err.message || "Some error"
        });
    }
}

exports.post_logout = (req, res) => {
    try {
        const {login} = req.body;
        if (login == null) {
            return res.status(400).json({"error": `missing login`})
        }
        let users = JSON.parse(JSON.stringify(exports.usersJSON))

        if (users.logins.indexOf(login) === -1) {
            return res.status(403).json({message: `User with login ${login} doesn't exist`})
        }

        res.json({
            "login": login,
            "access_level": 0,
            "message": "logout successful"
        })
    } catch (err) {
        res.status(500).json({
            message:
                err.message || "Some error"
        });
    }
}

