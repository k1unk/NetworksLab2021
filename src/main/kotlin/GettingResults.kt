import io.ktor.util.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun getResults(token: String) {
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/game/result"))
        .header("Authorization", token)
        .build();

    val response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
        val responseBody = response.body()
        var result = ""
        var win = ""
        for (i in 0..responseBody.length - 2) {
            var value = parserWithoutQuotes(responseBody, i, 't')
            if (value != "") result = value
            value = parserWithoutQuotes(responseBody, i, 'n')
            if (value != "") win = value
        }
        println("Результат розыгрыша: $result, баланс: $win")
    } else println(response)
}

