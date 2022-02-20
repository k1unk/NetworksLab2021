import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun makeBet(token: String) {
    val type: Int
    while (true) {
        println("Выберите тип ставки: чет/нечет - 1, число - 2")
        val typeString = readLine()
        if (typeString == "1" || typeString == "2") {
            type = typeString.toInt()
            break
        }
    }
    var number = 0
    if (type == 1) {
        while (true) {
            println("Выберите чет/нечет: чет - 1, нечет - 2")
            val numberString = readLine()
            if (numberString == "1" || numberString == "2") {
                number = numberString.toInt()
                break
            }
        }
    }
    if (type == 2) {
        while (true) {
            println("Выберите число от 0 до 36")
            val numberString = readLine()
            try {
                number = numberString!!.toInt()
                if (number in 0..36) break
            } catch (e: Exception) { }
        }
    }

    var count: Int
    while (true) {
        println("Выберите сумму ставки")
        val countString = readLine()
        try {
            count = countString!!.toInt()
            break
        } catch (e: Exception) { }
    }
    postBet(count, type, number, token)
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