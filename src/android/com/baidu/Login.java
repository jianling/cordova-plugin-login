/**
 * Baidu passport Login plugin for Cordova / Phonegap
 */
package com.baidu;


import android.content.Intent;
import android.webkit.CookieManager;

import com.baidu.bce.LoginActivity;
import com.baidu.sapi2.SapiAccountManager;
import com.baidu.sapi2.SapiConfiguration;
import com.baidu.sapi2.utils.enums.Domain;
import com.baidu.sapi2.utils.enums.LoginShareStrategy;
import com.synconset.CordovaHttpGet;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends CordovaPlugin {


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        if (action.equals("showLoginView")) {
            SapiConfiguration config = new SapiConfiguration.Builder(cordova.getActivity())
                    .setProductLineInfo("bceplat", "1", "42jdt3wa1n9g4o3sk3hgtt53x0d3cr86")
                    .setRuntimeEnvironment(Domain.DOMAIN_ONLINE)
                    // 安全SDK key, 授权邮件中ssdkAppkey对应sofireAppKey，ssdkSecretKey对应sofireSecKey，WHO对应sofireHostID
                    .sofireSdkConfig("200011", "71eb84d6dca8a124fd30685fb3a20acb", 1)
                    // 配置初始互通策略，该配置项仅在服务端互通策略未同步完成时候有效，6.6.0及以上版本互通策略由pass统一控制，
                    // 默认全部为静默互通（首次静默+选择互通）
                     .initialShareStrategy(LoginShareStrategy.SILENT)
                    // 调试模式设置
                    .debug(true)
                    .build();
            // 初始化组件
            SapiAccountManager.getInstance().init(config);

            final CordovaInterface cordovaInt = cordova;

            Intent intent = new Intent(cordova.getActivity(), LoginActivity.class);
            cordova.startActivityForResult(new CordovaPlugin() {
                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent intent) {
                    if (intent != null) {
                        String bduss = intent.getExtras().getString("bduss");

                        String loginUrl =
                                "https://login.bce.baidu.com/postlogin?_1495851167415&redirect=http%3A%2F%2Fconsole.bce.baidu.com";
                        Map paramsMap = new HashMap<String, String>();
                        Map headersMap = new HashMap<String, String>();

                        headersMap.put("Cookie", "BDUSS=" + bduss + "; bce-login-type=PASSPORT;");

                        PostLogin get = new PostLogin(loginUrl, paramsMap, headersMap, callbackContext);
                        cordovaInt.getThreadPool().execute(get);
                    }
                }
            }, intent, 1);
        }
        else if (action.equals("getCookie")) {
            String url = args.getString(0);

            try {
                CookieManager cookieManager = CookieManager.getInstance();

                String cookies = cookieManager.getCookie(url);

                JSONObject cookiesObject = new JSONObject();
                cookiesObject.put("cookie", cookies);

                callbackContext.success(cookiesObject);
            } catch (Exception e) {
                callbackContext.error("");
            }
        }
        else if (action.equals("getCookieValue")) {
            String url = args.getString(0);
            String key = args.getString(1);

            try {
                CookieManager cookieManager = CookieManager.getInstance();
                String[] cookies = cookieManager.getCookie(url).split("; ");
                String cookieValue = "";

                for (String cookie : cookies) {
                    if (cookie.contains(key + "=")) {
                        cookieValue = cookie.split("=")[1].trim();
                        break;
                    }
                }

                JSONObject cookieValueObject = new JSONObject();
                cookieValueObject.put("cookieValue", cookieValue);

                callbackContext.success(cookieValueObject);
            } catch (Exception e) {
                callbackContext.error("");
            }
        }
        else if (action.equals("logout")) {
            String logoutUrl = "https://login.bce.baidu.com/logout";
            Map paramsMap = new HashMap<String, String>();
            Map headersMap = new HashMap<String, String>();

            CordovaHttpGet get = new CordovaHttpGet(logoutUrl, paramsMap, headersMap, callbackContext);
            cordova.getThreadPool().execute(get);
        }

        return true;
    }

}
