/**
 * Multiview plugin for Cordova / Phonegap
 */

package com.baidu.bce;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginEntry;

import java.util.ArrayList;

public class LoginActivity extends Activity {

    public String message;
    private String callbackId;
    ConfigXmlParser parser = new ConfigXmlParser();
    protected ArrayList<PluginEntry> pluginEntries = parser.getPluginEntries();
    protected CordovaPreferences preferences = parser.getPreferences();
    protected CordovaWebView appView;
    protected CordovaInterfaceImpl cordovaInterface = new CordovaInterfaceImpl(this) { };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        callbackId = bundle.getString("callbackId");

        super.onCreate(savedInstanceState);

        appView = makeWebView();

        pluginEntries.add(new PluginEntry("Login", "com.baidu.Login", true));

        appView.init(cordovaInterface, pluginEntries, preferences);

        cordovaInterface.onCordovaInit(appView.getPluginManager());

        createPageViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        cordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    protected CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(this, preferences);
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        return dm.widthPixels;
    }

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        return dm.heightPixels;
    }

    private void createPageViews() {
        // Main container layout
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        // background layout
        Drawable bgImg = getResources().getDrawable(R.drawable.screen);
        RelativeLayout bg = new RelativeLayout(this);
        bg.setBackground(bgImg);
        bg.setLayoutParams(
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT)
        );
        bg.setHorizontalGravity(Gravity.START);
        bg.setVerticalGravity(Gravity.TOP);

        // close button
        ImageView closeBtn = new ImageView(this);
        closeBtn.setImageResource(R.drawable.login_close);
        RelativeLayout.LayoutParams closeBtnLP =
                new RelativeLayout.LayoutParams(dpToPixels(30), dpToPixels(30));
        closeBtnLP.setMargins(
                getScreenWidth() - dpToPixels(80),
                dpToPixels(60),
                0,
                0);
        closeBtn.setLayoutParams(closeBtnLP);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cordovaInterface.getActivity().finish();
            }
        });
        bg.addView(closeBtn);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.argb ( 25,  255,  255,  255 ));
        gd.setCornerRadius(dpToPixels(50));

        // UC
        RelativeLayout ucLoginBtn = new RelativeLayout(this);
        ucLoginBtn.setBackground(gd);
        RelativeLayout.LayoutParams ucLoginBtnLP =
                new RelativeLayout.LayoutParams(getScreenWidth() * 9 / 10, dpToPixels(100));
        ucLoginBtnLP.setMargins(
                getScreenWidth() / 20,
                getScreenHeight() / 2 - dpToPixels(140),
                getScreenWidth() / 20,
                0);
        ucLoginBtn.setLayoutParams(ucLoginBtnLP);
        ucLoginBtn.setHorizontalGravity(Gravity.START);
        ucLoginBtn.setVerticalGravity(Gravity.CENTER_VERTICAL);
        ucLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                cordovaInterface.pluginManager.exec("Login", "showUCLoginView", callbackId, "[]");
                Intent intent = new Intent();
                intent.putExtra("loginType", "uc");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ImageView ucLogo = new ImageView(this);
        ucLogo.setImageResource(R.drawable.bce_icon);
        RelativeLayout.LayoutParams ucLogoLP =
                new RelativeLayout.LayoutParams(dpToPixels(60), dpToPixels(60));
        ucLogoLP.setMargins(
                dpToPixels(40),
                dpToPixels(0),
                0,
                0);
        ucLogo.setLayoutParams(ucLogoLP);
        ucLoginBtn.addView(ucLogo);

        TextView ucTitleView = new TextView(this);
        ucTitleView.setText("百度云账号");
        ucTitleView.setTextColor(Color.WHITE);
        ucTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.login_title_size));
        ucTitleView.setPadding(dpToPixels(150), 0, 0, 0);
        ucLoginBtn.addView(ucTitleView);

        TextView ucTipView = new TextView(this);
        ucTipView.setText("原推广账号可直接登录");
        ucTipView.setTextColor(Color.argb ( 255,  255,  144,  0 ));
        ucTipView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.login_tip_size));
        ucTipView.setPadding(dpToPixels(150), dpToPixels(35), 0, 0);
        ucLoginBtn.addView(ucTipView);


        // Add the back button、title to toolbar
        bg.addView(ucLoginBtn);

        // Pass
        RelativeLayout passLoginBtn = new RelativeLayout(this);
        passLoginBtn.setBackground(gd);
        RelativeLayout.LayoutParams passLoginBtnLP =
                new RelativeLayout.LayoutParams(getScreenWidth() * 9 / 10, dpToPixels(100));
        passLoginBtnLP.setMargins(
                getScreenWidth() / 20,
                getScreenHeight() / 2 + dpToPixels(10),
                getScreenWidth() / 20,
                0);
        passLoginBtn.setLayoutParams(passLoginBtnLP);
        passLoginBtn.setHorizontalGravity(Gravity.START);
        passLoginBtn.setVerticalGravity(Gravity.CENTER_VERTICAL);
        passLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                cordovaInterface.pluginManager.exec("Login", "showPassLoginView", callbackId, "[]");
                Intent intent = new Intent();
                intent.putExtra("loginType", "pass");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ImageView passLogo = new ImageView(this);
        passLogo.setImageResource(R.drawable.pass_icon);
        RelativeLayout.LayoutParams passLogoLP =
                new RelativeLayout.LayoutParams(dpToPixels(60), dpToPixels(60));
        passLogoLP.setMargins(
                dpToPixels(40),
                dpToPixels(0),
                0,
                0);
        passLogo.setLayoutParams(passLogoLP);
        passLoginBtn.addView(passLogo);

        TextView passTitleView = new TextView(this);
        passTitleView.setText("百度账号");
        passTitleView.setTextColor(Color.WHITE);
        passTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.login_title_size));
        passTitleView.setPadding(dpToPixels(150), 0, 0, 0);
        passLoginBtn.addView(passTitleView);

        TextView passTipView = new TextView(this);
        passTipView.setText("原百度账号可直接登录");
        passTipView.setTextColor(Color.argb ( 255,  255,  144,  0 ));
        passTipView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.login_tip_size));
        passTipView.setPadding(dpToPixels(150), dpToPixels(35), 0, 0);
        passLoginBtn.addView(passTipView);

        // Add the back button、title to toolbar
        bg.addView(passLoginBtn);

        // Add our toolbar to our main view/layout
        main.addView(bg);

        addContentView(main, new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
    private int dpToPixels(int dipValue) {
        return (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                (float) dipValue,
                this.getResources().getDisplayMetrics()
        );
    }

}
