import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun getLoginData(): List<String> {
    println("Введите логин")
    val login = readLine()
    println("Введите пароль")
    val password = readLine()
    println("Вы крупье или игрок? (Если крупье - введите 1, если игрок - введите 2)")
    var isAdmin = false
    while (true) {
        val isAdminStr = readLine()
        if (isAdminStr == "1") {
            isAdmin = true
            break
        } else if (isAdminStr == "2") break
        else {
            println("Вы крупье или игрок? (Если крупье - введите 1, если игрок - введите 2)")
        }
    }

    return listOf(login.toString(), password.toString(), isAdmin.toString())


/*
       val zxc = readLine()
       if (zxc=="1") return listOf("player1", "123", "false")
       if (zxc=="2") return listOf("player2", "123", "false")
       return listOf("Ilia", "qwerty123", "true")
*/

}

fun auth(): Pair<String, Boolean> {
    var token: String
    var isAdmin: Boolean
    while (true) {
        val (login, password, isAdminString) = getLoginData()
        isAdmin = isAdminString.toBoolean()
        token = postLogin(login, password, isAdmin)
        if (token != "") {
            var role = "Игрок"
            if (isAdmin) role = "Крупье"
            println("Вы зарегистрировались! Ваш никнейм: $login. Ваша роль: $role")
            break
        }
    }
    return Pair(token, isAdmin)
}

fun postLogin(login: String, password: String, isAdmin: Boolean): String {
    val values = mapOf("name" to login, "password" to password, "admin" to isAdmin)

    val requestBody: String = ObjectMapper().writeValueAsString(values)

    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/auth/login"))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
        return response.headers().allValues("authorization")[0]
    }
    if (response.statusCode() == 401) {
        println("Логин, пароль или роль указаны неверно")
        return ""
    }
    return ""
}

