import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class githubUserActivityApp {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java githubUserActivityApp <username>");
            return;
        }

        String url = "https://api.github.com/users/" + args[0] + "/events";
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Accept", "application/vnd.github+json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonResponse = response.body();
                printActivities(jsonResponse);
            } else {
                System.out.println("Error: " + response.statusCode());
            }

        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printActivities(String jsonResponse) {
        String[] events = jsonResponse.split("\\},\\{");

        for (String event : events) {
            String type = getJsonValue(event, "\"type\":\"([^\"]+)\"");
            String repoName = getJsonValue(event, "\"repo\":\\{\"id\":[^,]+,\"name\":\"([^\"]+)\"");
            String actionDescription = "";

            switch (type) {
                case "PushEvent":
                    String commits = getJsonValue(event, "\"size\":([0-9]+)");
                    actionDescription = "Pushed " + commits + " commit(s) to " + repoName;
                    break;
                case "IssuesEvent":
                    String action = getJsonValue(event, "\"action\":\"([^\"]+)\"");
                    actionDescription = capitalize(action) + " an issue in " + repoName;
                    break;
                case "WatchEvent":
                    actionDescription = "Starred " + repoName;
                    break;
                case "CreateEvent":
                    String refType = getJsonValue(event, "\"ref_type\":\"([^\"]+)\"");
                    String ref = getJsonValue(event, "\"ref\":\"([^\"]+)\"");
                    if ("repository".equals(refType)) {
                        actionDescription = "Created a new repository " + repoName;
                    } else if ("branch".equals(refType)) {
                        actionDescription = "Created a new branch " + ref + " in " + repoName;
                    } else if ("tag".equals(refType)) {
                        actionDescription = "Created a new tag " + ref + " in " + repoName;
                    }
                    break;
                case "ForkEvent":
                    actionDescription = "Forked " + repoName;
                    break;
                case "PullRequestEvent":
                    String prAction = getJsonValue(event, "\"action\":\"([^\"]+)\"");
                    actionDescription = capitalize(prAction) + " a pull request in " + repoName;
                    break;
                case "IssueCommentEvent":
                    actionDescription = "Commented on an issue in " + repoName;
                    break;
                default:
                    actionDescription = type.replace("Event", "") + " in " + repoName;
                    break;
            }

            if (!actionDescription.isEmpty() && !repoName.isEmpty()) {
                System.out.println("- " + actionDescription);
            }
        }
    }

    private static String getJsonValue(String json, String regexPattern) {
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
