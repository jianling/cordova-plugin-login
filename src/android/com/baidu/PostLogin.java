package com.baidu;

import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.CookieManager;
import android.content.Context;

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
    private Context cordovaContext;

    public PostLogin(String urlString,
                     Map<?, ?> params,
                     Map<String, String> headers,
                     CallbackContext callbackContext,
                     Context cordovaContext) {
        super(urlString, params, headers, callbackContext);

        this.cordovaContext = cordovaContext;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.get(this.getUrlString(), this.getParams(), false);

            request.followRedirects(false);
            this.setupSecurity(request);
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());

            // reset User_agent: bai du yun/1.1.0 (Google; Android 8.0.0)
            String versionCode = "";
            String packageName = cordovaContext.getPackageName();
            try {
                versionCode = cordovaContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                // e
            }

            String agent = "bai du yun/" + versionCode
                    + " (" + Build.MANUFACTURER + "; Android " + Build.VERSION.RELEASE + ")";

            request.userAgent(agent);

            int code = request.code();
            JSONObject response = new JSONObject();
            this.addResponseHeaders(request, response);
            response.put("status", code);
            if (code == 302) {
                List<String> cookies = request.getConnection().getHeaderFields().get("Set-Cookie");

                List<String> location = request.getConnection().getHeaderFields().get("Location");

                if (!location.get(0).contains("console.bce.baidu.com")) {
                    this.getCallbackContext().error(response);
                    return;
                }

                if (!cookies.isEmpty()) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    for (String cookie : cookies) {
                        cookieManager.setCookie("https://console.bce.baidu.com", cookie);
                    }
                }

                // 登录成功
                this.getCallbackContext().success(response);
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