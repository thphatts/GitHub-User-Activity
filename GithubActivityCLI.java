import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GithubActivityCLI {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GithubActivity <username>");
            return;
        }
        String username = args[0];
        final String GITHUB_URL = "https://api.github.com/users/" + username + "/events/public";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_URL)).header("accept", "application/vnd.github+json").GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                System.out.println("Error: Username not found");
                return;
            } else if (response.statusCode() == 403) {
                System.out.println("Error: Rate Limits");
                return;
            } else if (response.statusCode() == 200) {
                parseAndPrintEvents(response.body(), username);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            interruptedException.printStackTrace();
        }
    }

    public static void parseAndPrintEvents(String json, String username) {
        String[] blocks = json.split("\"type\":\"");
        for (int i = 1; i < blocks.length; i++) {
            String block = blocks[i];
            int endOfType = block.indexOf("\"");
            String type = block.substring(0, endOfType);
            String repoName = "Repo not found";
            int repoIndex = block.indexOf("\"repo\":");

            if (repoIndex != -1) {
                int nameKeyIndex = block.indexOf("\"name\":\"", repoIndex);
                if (nameKeyIndex != -1) {
                    int startName = nameKeyIndex + 8;
                    int endName = block.indexOf("\"", startName);

                    repoName = block.substring(startName, endName);
                }
            }
            String action;
            switch (type) {
                case "PushEvent": {
                    action = "Pushed 1 commit to " + repoName;
                    break;
                }
                case "CreateEvent": {
                    action = "Created in " + repoName;
                    break;
                }
                case "PullRequestEvent": {
                    action = "Sent a pull request to " + repoName;
                    break;
                }
                case "StarEvent": {
                    action = "Starred " + repoName;
                    break;
                }
                case "ForkEvent": {
                    action = "Forked " + repoName;
                }
                default: {
                    action = type + " " + repoName;
                    break;
                }

            }
            System.out.println(action);
        }
    }

}