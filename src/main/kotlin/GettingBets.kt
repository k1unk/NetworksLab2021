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
        println(result)
    } else println(response)
}

