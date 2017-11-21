/**
 * Multiview plugin for Cordova / Phonegap
 */

package com.baidu.bce;

import android.graphics.Color;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.baidu.MultiviewActivity;

public class UcLoginActivity extends MultiviewActivity {

    @Override
    protected void init() {
        super.init();

        createRegisterButton();
    }

    private void createRegisterButton() {
        Button registerBtn = new Button(this);
        registerBtn.setText("注册");
        registerBtn.setBackgroundColor(Color.TRANSPARENT);
        registerBtn.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        registerBtn.setLayoutParams(layoutParams);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cordovaInterface.pluginManager.exec("Login", "showUCRegisterView", "", "[]");
            }
        });

        // Add to toolbar
        toolbar.addView(registerBtn);
    }



}
