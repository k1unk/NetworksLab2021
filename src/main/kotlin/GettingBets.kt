import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun getBets(token: String) {
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/game/info"))
        .header("Authorization", token)
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
        val result = response.body()
        if (result == "[]") {
            println("Ставки отсутствуют")
            return
        }

        printResults(result)
    } else println(response)
}

fun printResults(result: String) {
    val bets = mutableListOf<String>()
    val types = mutableListOf<String>()
    val numbers = mutableListOf<String>()
    for (i in 0..result.length - 2) {
        val bet = parser(result, i, 't')
        val type = parser(result, i, 'e')
        val number = parser(result, i, 'r')
        if (type != "")
            types.add(type)
        if (bet != "")
            bets.add(bet)
        if (number != "")
            numbers.add(number)
    }

    println("Ваши ставки:")
    for (i in 0 until types.size) {
        println("Тип: ${types[i]}, номер: ${numbers[i]}, сумма ставки: ${bets[i]}")
    }
}

