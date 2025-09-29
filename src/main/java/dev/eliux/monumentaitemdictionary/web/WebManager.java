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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
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

    public static void manageRequestAsynchronous(String targetUrl, Consumer<String> onSuccess, Runnable onFailure) {
        new Thread(() -> {
            ChatHud chat = MinecraftClient.getInstance().inGameHud.getChatHud();
            Style style = Style.EMPTY.withColor(Formatting.RED);

            try {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(targetUrl)).timeout(Duration.ofMinutes(1)).GET().build();
                HttpResponse<String> response = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .get(20, TimeUnit.SECONDS);
                onSuccess.accept(response.body());
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