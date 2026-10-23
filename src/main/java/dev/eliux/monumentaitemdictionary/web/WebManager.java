package dev.eliux.monumentaitemdictionary.web;

import dev.eliux.monumentaitemdictionary.util.Styles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

import java.net.URI;
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

public class WebManager {

    public static void manageRequestsAsynchronous(Runnable onSuccess, Runnable onFailure, WebRequest... requests) {
        ChatHud chat = MinecraftClient.getInstance().inGameHud.getChatHud();

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
                chat.addMessage(Text.literal("Monumenta Item Dictionary data request is finished.").setStyle(Styles.GREEN));
                return;
            }
            catch (InterruptedException e) {
                chat.addMessage(Text.literal("Monumenta Item Dictionary data request was interrupted.").setStyle(Styles.RED));
            }
            catch (ExecutionException e) {
                chat.addMessage(Text.literal("An error occurred during an Monumenta Item Dictionary data request.").setStyle(Styles.RED));
                e.printStackTrace();
            }
            catch (TimeoutException e) {
                chat.addMessage(Text.literal("Monumenta Item Dictionary data request timed out, API might be unreachable. Try again later.").setStyle(Styles.RED));
            }
            onFailure.run();
        }).start();
    }
}