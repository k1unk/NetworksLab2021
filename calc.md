### Registration

Request
```
POST /auth

{  
    "login": login,  
    "password": password  
}  
```
OK Response
```
200 

{
    "login": login
}  
```
Error Response
```http request
403

{"message": "User with login ${login} already exists"}
```

### Login

Request
```
POST /login

{  
    "login": login,  
    "password": password  
}  
```
OK Response
```
200 

{
    "token": token,
    "user": {
        "login": login,
        "access_level": access_level
    }
}  
```
Error Response
```http request
403

{"message": "User with login ${login} doesn't exist"} 

{"message": "Invalid password"} 
```

### Logout

Request
```
POST /login

{  
    "login": login
}  
```
OK Response
```
200 

{
    "login": login,
    "access_level": access_level
    "message": "logout successful"
}
```
Error Response
```http request
403

{"message": "User with login ${login} doesn't exist"}
```

### plus, minus, mult, div

Request
```
GET /plus?o1={Value}&o2={Value}&authorization={Value}
GET /minus?o1={Value}&o2={Value}&authorization={Value}
GET /mult?o1={Value}&o2={Value}&authorization={Value}
GET /div?o1={Value}&o2={Value}&authorization={Value}

```
OK Response
```
200 

{
    "o1": o1,
    "o2": o2,
    "result": result
}
```
Error Response
```http request
401

{"message": "no access"}
```


### factorial, sqrt

Request
```
GET /fact?o1={Value}&authorization={Value}
GET /sqrt?o1={Value}&authorization={Value}

```
OK Response
```
200 

{
    "o1": o1,
    "result": result
}
```
Error Response
```http request
401

{"message": "no access"}
```

