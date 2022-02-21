const express = require("express");
const login = require("./login");
const app = require('express')()
const {fast_func, slow_func} = require("./operations");

const host = 'localhost'
const port = 8080
app.use(express.json())

app.post('/auth', (req, res) => {
    login.post_auth(req, res)
})
app.post('/login', (req, res) => {
    login.post_login(req, res)
})
app.post('/logout', (req, res) => {
    login.post_logout(req, res)
})

app.get('/plus', (req, res) => {
    fast_func(req, res, "+")
})
app.get('/minus', (req, res) => {
    fast_func(req, res, "-")
})
app.get('/mult', (req, res) => {
    fast_func(req, res, "*")
})
app.get('/div', (req, res) => {
    fast_func(req, res, "/")
})
app.get('/sqrt', async (req, res) => {
    await slow_func(req, res, "sqrt")
})
app.get('/fact', async (req, res) => {
    await slow_func(req, res, "fact")
})

app.listen(port);
