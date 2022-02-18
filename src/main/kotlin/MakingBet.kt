import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun makeBet(token: String) {
    println("Выберите тип ставки: чет/нечет - 1, число - 2")
    val type = readLine()?.toInt()
    var number = 0
    if (type == 1) {
        println("Выберите чет/нечет: чет - 1, нечет - 2")
        number = readLine()?.toInt()!!
    }
    if (type == 2) {
        println("Выберите число от 0 до 35")
        number = readLine()?.toInt()!!
    }
    println("Выберите сумму ставки")
    val count = readLine()?.toInt()
    postBet(count!!, type!!, number, token)
}


fun postBet(count: Int, type: Int, number: Int, token: String) {
    var typeString = ""
    if (type == 1 && number == 1) {
        typeString = "odd"
    }
    if (type == 1 && number == 2) {
        typeString = "even"
    }
    if (type == 2) {
        typeString = "number"
    }
    postBet(count, typeString, number, token)
}

fun postBet(count: Int, type: String, number: Int, token: String) {
    val values = mapOf("bet" to count, "type" to type, "number" to number)

    val requestBody: String = ObjectMapper().writeValueAsString(values)

    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/game/bet"))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .header("Authorization", token)
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
        println("ставка сделана")
    } else println(response)
}