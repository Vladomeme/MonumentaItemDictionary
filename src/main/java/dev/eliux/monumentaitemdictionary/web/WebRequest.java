package dev.eliux.monumentaitemdictionary.web;

import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WebRequest {

    final String url;
    CompletableFuture<HttpResponse<String>> future;

    public WebRequest(String url) {
        this.url = url;
    }

    public boolean isSuccess() {
        return !future.isCancelled() && !future.isCompletedExceptionally();
    }

    public String result() {
        if (isSuccess()) {
            try {
                return Objects.requireNonNullElse(future.get().body(), "{}");
            }
            catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return "{}";
    }
}
