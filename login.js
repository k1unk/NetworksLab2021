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

exports.check_login_and_password = (res, login, login_err_msg, password, password_err_msg) => {
    let users = exports.usersJSON

    if (users.logins.indexOf(login) === -1) {
        res.status(403).json({message: login_err_msg})
        return false
    }

    let user = users.users[Object.keys(users.users).find(user => users.users[user].login === login)]

    if (user.password !== password) {
        res.status(403).json({message: password_err_msg})
        return false
    }
    return user
}

exports.post_auth = (req, res) => {
    try {
        const {login, password} = req.body;
        if (login == null || password == null) {
            return res.status(400).json({"error": `missing login or password`})
        }
        let users = exports.usersJSON

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
    } catch (err) {
        res.status(500).json({
            message: err.message || "Some error"
        });
    }
}


exports.post_login = (req, res) => {
    try {
        const {login, password} = req.body;
        if (login == null || password == null) {
            return res.status(400).json({"error": `missing login or password`})
        }
        let user = exports.check_login_and_password(res, login,
            `User with login ${login} doesn't exist`, password,  "Invalid password")

        if (!user) return

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
            message: err.message || "Some error"
        });
    }
}

exports.post_logout = (req, res) => {
    try {
        const {login} = req.body;
        if (login == null) {
            return res.status(400).json({"error": `missing login`})
        }
        let users = exports.usersJSON

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
            message: err.message || "Some error"
        });
    }
}

