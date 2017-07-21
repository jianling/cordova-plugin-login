package com.baidu;

import android.webkit.CookieManager;

import com.github.kevinsawicki.http.HttpRequest;
import com.synconset.CordovaHttp;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by jianling on 2017/7/20.
 */

public class PostLogin extends CordovaHttp implements Runnable {
    public PostLogin(String urlString, Map<?, ?> params, Map<String, String> headers, CallbackContext callbackContext) {
        super(urlString, params, headers, callbackContext);
    }

    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.get(this.getUrlString(), this.getParams(), false);

            request.followRedirects(false);
            this.setupSecurity(request);
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());
            int code = request.code();
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
            this.addResponseHeaders(request, response);
            response.put("status", code);
            if (code == 302) {
                List<String> cookies = request.getConnection().getHeaderFields().get("Set-Cookie");

                if (!cookies.isEmpty()) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    for (String cookie : cookies) {
                        cookieManager.setCookie("https://console.bce.baidu.com", cookie);
                    }
                }

                // 登录成功
                this.getCallbackContext().success(response);

                // TODO 登录失败
            }
        } catch (JSONException e) {
            this.respondWithError("There was an error generating the response");
        } catch (HttpRequest.HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(0, "The host could not be resolved");
            } else if (e.getCause() instanceof SSLHandshakeException) {
                this.respondWithError("SSL handshake failed");
            } else {
                this.respondWithError("There was an error with the request");
            }
        }
    }
}