const login = require("./login");
const jwt_decode = require("jwt-decode");

const check_auth = (req, res) => {
    try {
        let decoded = jwt_decode(req.headers.authorization.substring(7));
       // let decoded = jwt_decode(req.headers.authorization);
        if (decoded == null) {
            res.status(400).json({"error": `missing authorization`})
            return false
        }
        if (!login.check_login_and_password(res, decoded.login,
            `no access`, decoded.password, `no access`)) return

    } catch (err) {
        res.status(500).json({
            message: err.message || "Some error"
        });
        return false
    }
    return true

}
exports.fast_func = (req, res, operation) => {

    if (check_auth(req, res))

        try {
            const o1 = Number(req.query.o1)
            const o2 = Number(req.query.o2)
            if (o1 == null || o2 == null) {
                return res.status(400).json({"error": `missing o1 or o2`})
            }
            if (operation === "/" && o2 === 0) {
                res.status(403).json({
                    message: "div 0 error"
                });
            }
            let result = o1
            switch (operation) {
                case "+":
                    result += o2
                    break
                case "-":
                    result -= o2
                    break
                case "*":
                    result *= o2
                    break
                case "/":
                    result /= o2
                    break
            }
            res.json({
                o1: o1,
                o2: o2,
                result: result
            })
        } catch (err) {
            res.status(500).json({
                message: err.message || "Some error"
            });
        }
};

exports.slow_func = async (req, res, operation) => {

    if (check_auth(req, res))

        try {
            setTimeout(async () => {
                try {
                    const o1 = Number(req.query.o1)
                    if (o1 == null) {
                        return res.status(400).json({"error": `missing o1`})
                    }
                    await slow_operation(o1, operation).then(data => {
                        res.json({
                            o1,
                            result: data
                        });
                    })
                        .catch(err => {
                            res.status(500).json({
                                message: err.message || `Some error during slow_operation`
                            });
                        });
                } catch (err) {
                    res.status(500).json({
                        message: err.message || "Some error during slow_operation"
                    });
                }
            }, 5000)
        } catch (err) {
            res.status(500).json({
                message: err.message || "Some error"
            });
        }
}

const factorial = (num) => {
    let result = 1;
    for (let i = 2; i <= num; i++)
        result = result * i;
    return result;
}

const slow_operation = async (a, operation) => {
    let result = 0
    switch (operation) {
        case "sqrt":
            result = Math.sqrt(a)
            break
        case "fact":
            result = factorial(a)
            break
    }
    return result
};
