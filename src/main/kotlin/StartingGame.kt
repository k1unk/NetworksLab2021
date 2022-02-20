import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun startGame(token: String) {
    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:4567/game/gamble"))
        .header("Authorization", token)
        .build();

    val response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
        println(response.body())
    } else println(response)
}

