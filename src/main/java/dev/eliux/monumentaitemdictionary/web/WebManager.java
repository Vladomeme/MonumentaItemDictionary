package dev.eliux.monumentaitemdictionary.web;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class WebManager {
    public static String getRequestSynchronous(String targetUrl) throws IOException {
        URL url = new URL(targetUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        if (con.getResponseCode() != 200) {
            System.out.println("By no problem I meant: no, problem!");
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

        return br.lines().collect(Collectors.joining());
    }

    public static void manageRequestsAsynchronous(Runnable onSuccess, Runnable onFailure, WebRequest... requests) {
        ChatHud chat = MinecraftClient.getInstance().inGameHud.getChatHud();
        Style style = Style.EMPTY.withColor(Formatting.RED);

        new Thread(() -> {
            try (HttpClient client = HttpClient.newHttpClient()) {
                List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>(requests.length);

                for (WebRequest request : requests) {
                    CompletableFuture<HttpResponse<String>> future = client.sendAsync(
                            HttpRequest.newBuilder().uri(URI.create(request.url)).timeout(Duration.ofMinutes(1)).GET().build(),
                            HttpResponse.BodyHandlers.ofString());
                    futures.add(future);
                    request.future = future;
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(60, TimeUnit.SECONDS);
                onSuccess.run();
                chat.addMessage(Text.literal("Monumenta Item Dictionary data request is finished.").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
                return;
            }
            catch (InterruptedException e) {
                chat.addMessage(Text.literal("Monumenta Item Dictionary data request was interrupted.").setStyle(style));
            }
            catch (ExecutionException e) {
                chat.addMessage(Text.literal("An error occurred during an Monumenta Item Dictionary data request.").setStyle(style));
                e.printStackTrace();
            }
            catch (TimeoutException e) {
                chat.addMessage(Text.literal("Monumenta Item Dictionary data request timed out, API might be unreachable. Try again later.").setStyle(style));
            }
            onFailure.run();
        }).start();
    }
}