import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class githubUserActivityApp {
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println("Usage: java githubUserActivityApp <username>");
            return;
        }

        String URL = "https://api.github.com/users/" + args[0] + "/events";

        HttpClient client = HttpClient.newHttpClient();

        try{
            HttpRequest request = HttpRequest.newBuilder().uri(new URI(URL)).header("Accept", "application/vnd.github+json").GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200){
                System.out.println(response.body());
            } else {
                System.out.println("Error:" + response.statusCode());
            }
        } catch (URISyntaxException uriSyntaxException){
            uriSyntaxException.printStackTrace();
        } catch (IOException ioException){
            ioException.printStackTrace();
        } catch (InterruptedException interruptedException){
            Thread.currentThread().interrupt();
            interruptedException.printStackTrace();
        }
    }
}