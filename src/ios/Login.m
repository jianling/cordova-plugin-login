/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

#import <Cordova/CDVPlugin.h>
#import "Login.h"
#import "SAPIMainManager.h"
#import "SAPILoginViewController.h"
#import "AFHTTPSessionManager.h"
#import "TextResponseSerializer.h"

@interface Login() {
    NSString* callbackId;
}
@end

@implementation Login


// TODO 超时自动登录 或者定时刷新登录状态

- (void)getCookie:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    NSString* urlString = [command.arguments objectAtIndex:0];
    __block NSString* cookieStr = @"";

    if (urlString != nil) {
        NSArray* cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:[NSURL URLWithString:urlString]];

        [cookies enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            NSHTTPCookie *cookie = obj;

            cookieStr = [cookieStr stringByAppendingFormat:@"%@=%@; ",cookie.name,cookie.value];
        }];
    }

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"cookie": cookieStr}];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getCookieValue:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    NSString* urlString = [command.arguments objectAtIndex:0];
    __block NSString* cookieName = [command.arguments objectAtIndex:1];

    if (urlString != nil) {
        NSArray* cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:[NSURL URLWithString:urlString]];
        __block NSString *cookieValue;

        [cookies enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            NSHTTPCookie *cookie = obj;

            if([cookie.name isEqualToString:cookieName])
            {
                cookieValue = cookie.value;
                *stop = YES;
            }
        }];
        if (cookieValue != nil) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"cookieValue":cookieValue}];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"No cookie found"];
        }

    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"URL was null"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)showLoginView:(CDVInvokedUrlCommand*)command
{
    SAPILoginViewController *loginVC = [[SAPILoginViewController alloc] init];
    loginVC.hidesBottomBarWhenPushed = YES;

    if (self.viewController.navigationController == NULL) {
        UINavigationController *nav = [[UINavigationController alloc] init];

        self.webView.window.rootViewController = nav;
        [nav pushViewController:self.viewController animated:false];
        [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
    }

    [self.viewController.navigationController pushViewController:loginVC animated:YES];

    callbackId = command.callbackId;

    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"loginSuccess" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(loginSuccessCallback) name:@"loginSuccess" object:nil];
}

- (void)loginSuccessCallback
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
//    NSString* js = [NSString stringWithFormat:@"cordova.require('cordova/exec').nativeCallback('%@',%d,%@,%d, %d)", callbackId, YES, @"''", NO, YES];
//    [self.webViewEngine evaluateJavaScript:js completionHandler:nil];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"loginSuccess" object:nil];
}

- (void)logout:(CDVInvokedUrlCommand*)command
{
    SAPILoginModel *model = [SAPIMainManager sharedManager].currentLoginModel;
    [[SAPIMainManager sharedManager].loginService logout:model];

    [self postBceLogout:command];
}

- (void)postBceLogout:(CDVInvokedUrlCommand*)command {
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    manager.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeNone];

    manager.requestSerializer = [AFJSONRequestSerializer serializer];
    manager.responseSerializer = [TextResponseSerializer serializer];
    [manager GET:@"https://login.bce.baidu.com/logout" parameters:nil progress:nil
         success:^(NSURLSessionTask *task, id responseObject) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } failure:nil];
}


@end


