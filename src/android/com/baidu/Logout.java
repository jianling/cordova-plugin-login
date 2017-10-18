package com.baidu;

import android.os.Build;
import android.webkit.CookieManager;

import com.baidu.sapi2.SapiAccountManager;
import com.github.kevinsawicki.http.HttpRequest;
import com.synconset.CordovaHttp;

import org.apache.cordova.CallbackContext;

import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by jianling on 2017/7/31.
 */

public class Logout extends CordovaHttp implements Runnable {
    public Logout(String urlString, Map<?, ?> params, Map<String, String> headers, CallbackContext callbackContext) {
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
            if (code == 302) {
                // 退出 console
                this.getCallbackContext().success();
                // 退出 pass
                SapiAccountManager.getInstance().logout();
                // 清除 cookie
                if (Build.VERSION.SDK_INT >= 21) {
                    CookieManager.getInstance().removeAllCookies(null);
                }
                else {
                    CookieManager.getInstance().removeAllCookie();
                }
            }
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