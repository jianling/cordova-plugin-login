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
#import <PassportKit/SAPIMainManager.h>
#import <PassportKit/PASSLoginViewController.h>
#import "AFHTTPSessionManager.h"
#import "TextResponseSerializer.h"
#import "Multiview.h"
#import "PASSQRCodeScanViewController.h"

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

- (void)showPASSQRCodeScanViewController:(CDVInvokedUrlCommand*)command
{
    if (self.viewController.navigationController == NULL) {
        UINavigationController *nav = [[UINavigationController alloc] init];

        self.webView.window.rootViewController = nav;
        [nav pushViewController:self.viewController animated:false];
        [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
    }

    PASSQRCodeScanViewController *qrcodeVC = [[PASSQRCodeScanViewController alloc] init];
    qrcodeVC.hidesBottomBarWhenPushed = YES;
    [[UINavigationBar appearance] setTranslucent:NO];
    [[UINavigationBar appearance] setBarTintColor:[UIColor colorWithRed:25.0 / 255.0 green:35.0 / 255.0 blue:60.0 / 255.0 alpha:1]];

    UIColor *titleColor = [UIColor colorWithRed:255.0 / 255.0 green:255.0 / 255.0 blue:255.0 / 255.0 alpha:1];
    [[UINavigationBar appearance] setTitleTextAttributes:@{
                                                           NSForegroundColorAttributeName: titleColor
                                                           }];
    qrcodeVC.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"BackIcon"] style:UIBarButtonItemStylePlain target:self action:@selector(popView)];
    [qrcodeVC.navigationItem.leftBarButtonItem setTintColor:[UIColor whiteColor]];
    [self.viewController.navigationController pushViewController:qrcodeVC animated:YES];
}

- (void)showPASSAccountRealNameViewController:(CDVInvokedUrlCommand*)command
{
    if (self.viewController.navigationController == NULL) {
        UINavigationController *nav = [[UINavigationController alloc] init];

        self.webView.window.rootViewController = nav;
        [nav pushViewController:self.viewController animated:false];
        [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
    }

    PASSAccountRealNameViewController *realNameVC = [[PASSAccountRealNameViewController alloc] init];
    realNameVC.hidesBottomBarWhenPushed = YES;

    realNameVC.finishHandler = ^(NSDictionary *userInfo) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    };

    [self.viewController.navigationController pushViewController:realNameVC animated:YES];
}

- (void)showLoginView:(CDVInvokedUrlCommand*)command
{
    LoginViewController * viewController = [[LoginViewController alloc] init];

    UIView* background = [[UIView alloc] initWithFrame:CGRectMake(0, 0, viewController.view.bounds.size.width, viewController.view.bounds.size.height)];
    background.backgroundColor = [UIColor colorWithRed:0 / 255.0 green:0 / 255.0 blue:0 / 255.0 alpha:1];

    CGRect viewBounds = viewController.view.bounds;

    // 背景图
    // TODO 释放缓存
    UIImage* backgroundImage = [UIImage imageNamed:@"LaunchImage"];
    UIImageView* imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, viewBounds.size.width, viewBounds.size.height)];
    imageView.image = backgroundImage;
    [background addSubview:imageView];
    [viewController.view addSubview:background];

    // 关闭按钮
    UIImage* image = [UIImage imageNamed:@"CloseIcon"];
    UIButton* button = [[UIButton alloc] initWithFrame:CGRectMake(
                                                                  viewBounds.size.width * 0.8,
                                                                  viewBounds.size.height * 0.1,
                                                                  image.size.width / 2,
                                                                  image.size.height / 2)];
    [button setBackgroundImage:image forState:UIControlStateNormal];
    [button addTarget:self action:@selector(popView) forControlEvents:UIControlEventTouchDown];
    [viewController.view addSubview:button];

    // 百度云账号登录按钮
    UIButton* ucLoginBtn = [[UIButton alloc] initWithFrame:CGRectMake(
                                                                  viewBounds.size.width * 0.1,
                                                                  viewBounds.size.height * 0.5 - 100.f,
                                                                  viewBounds.size.width * 0.8,
                                                                  70.f)];
    [ucLoginBtn setBackgroundColor:[UIColor colorWithRed:255.0 / 255.0 green:255.0 / 255.0 blue:255.0 / 255.0 alpha:0.1]];
    ucLoginBtn.layer.cornerRadius = ucLoginBtn.bounds.size.height / 2;

//    UILabel* ucLoginTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(
//                                                                           ucLoginBtn.bounds.size.width * 0.4,
//                                                                           15.f,
//                                                                           ucLoginBtn.bounds.size.width * 0.5,
//                                                                           15.f)];
    UILabel* ucLoginTitleLabel = [[UILabel alloc] initWithFrame: CGRectZero];
    ucLoginTitleLabel.translatesAutoresizingMaskIntoConstraints = NO;
    ucLoginTitleLabel.backgroundColor = [UIColor clearColor];
    ucLoginTitleLabel.text = @"百度云账号";
    ucLoginTitleLabel.textColor = [UIColor whiteColor];
    ucLoginTitleLabel.font = [UIFont systemFontOfSize:15.f];
    [ucLoginBtn addSubview:ucLoginTitleLabel];

    NSLayoutConstraint* ucLoginTitleLabelTopConstraint = [NSLayoutConstraint constraintWithItem:ucLoginTitleLabel attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:ucLoginBtn attribute:NSLayoutAttributeTop multiplier:1.0f constant:15.0f];
    NSLayoutConstraint* ucLoginTitleLabelLeftConstraint = [NSLayoutConstraint constraintWithItem:ucLoginTitleLabel attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:ucLoginBtn attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:-40.0f];
    //iOS 6.0或者7.0调用addConstraints
    //[self.view addConstraints:@[leftConstraint, rightConstraint, topConstraint, heightConstraint]];

    //iOS 8.0以后设置active属性值
    ucLoginTitleLabelTopConstraint.active = YES;
    ucLoginTitleLabelLeftConstraint.active = YES;

//    UILabel* ucLoginTipLabel = [[UILabel alloc] initWithFrame:CGRectMake(
//                                                                           ucLoginBtn.bounds.size.width * 0.4,
//                                                                           40.f + viewBounds.size.height * 0.5 - 100.f,
//                                                                           ucLoginBtn.bounds.size.width * 0.5,
//                                                                           15.f)];
    UILabel* ucLoginTipLabel = [[UILabel alloc] initWithFrame: CGRectZero];
    ucLoginTipLabel.translatesAutoresizingMaskIntoConstraints = NO;
    ucLoginTipLabel.backgroundColor = [UIColor clearColor];
    ucLoginTipLabel.text = @"原推广账号可直接登录";
    ucLoginTipLabel.textColor = [UIColor colorWithRed:255.0 / 255.0 green:144.0 / 255.0 blue:0.0 / 255.0 alpha:1];
    ucLoginTipLabel.font = [UIFont systemFontOfSize:13.f];
    [ucLoginBtn addSubview:ucLoginTipLabel];

    NSLayoutConstraint* ucLoginTipLabelTopConstraint = [NSLayoutConstraint constraintWithItem:ucLoginTipLabel attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:ucLoginBtn attribute:NSLayoutAttributeTop multiplier:1.0f constant:40.0f];
    NSLayoutConstraint* ucLoginTipLabelLeftConstraint = [NSLayoutConstraint constraintWithItem:ucLoginTipLabel attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:ucLoginBtn attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:-40.0f];
    //iOS 6.0或者7.0调用addConstraints
    //[self.view addConstraints:@[leftConstraint, rightConstraint, topConstraint, heightConstraint]];

    //iOS 8.0以后设置active属性值
    ucLoginTipLabelTopConstraint.active = YES;
    ucLoginTipLabelLeftConstraint.active = YES;


    UIImage* UcLoginImage = [UIImage imageNamed:@"UcLoginIcon"];
    UIImageView* UCLoginIconIcon = [[UIImageView alloc] initWithFrame:CGRectMake(
                                                                                 ucLoginBtn.bounds.size.width * 0.2,
                                                                                 ucLoginBtn.bounds.size.height * 0.2,
                                                                                 (ucLoginBtn.bounds.size.height * 0.6) / UcLoginImage.size.height * UcLoginImage.size.width,
                                                                                 ucLoginBtn.bounds.size.height * 0.6)];
    UCLoginIconIcon.image = UcLoginImage;
    [ucLoginBtn addSubview:UCLoginIconIcon];

    [viewController.view addSubview:ucLoginBtn];
    [ucLoginBtn addTarget:self action:@selector(_showUCLoginView) forControlEvents:UIControlEventTouchDown];

    // 百度账号登录按钮
    UIButton* passLoginBtn = [[UIButton alloc] initWithFrame:CGRectMake(
                                                                      viewBounds.size.width * 0.1,
                                                                      viewBounds.size.height * 0.5 + 60.f,
                                                                      viewBounds.size.width * 0.8,
                                                                      70.f)];
    [passLoginBtn setBackgroundColor:[UIColor colorWithRed:255.0 / 255.0 green:255.0 / 255.0 blue:255.0 / 255.0 alpha:0.1]];
    passLoginBtn.layer.cornerRadius = passLoginBtn.bounds.size.height / 2;

//    UILabel* passLoginTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(
//                                                                           passLoginBtn.bounds.size.width * 0.4,
//                                                                           15.f,
//                                                                           passLoginBtn.bounds.size.width * 0.4,
//                                                                           15.f)];
    UILabel* passLoginTitleLabel = [[UILabel alloc] initWithFrame: CGRectZero];
    passLoginTitleLabel.translatesAutoresizingMaskIntoConstraints = NO;
    passLoginTitleLabel.backgroundColor = [UIColor clearColor];
    passLoginTitleLabel.text = @"百度账号";
    passLoginTitleLabel.textColor = [UIColor whiteColor];
    passLoginTitleLabel.font = [UIFont systemFontOfSize:15.f];
    [passLoginBtn addSubview:passLoginTitleLabel];

    NSLayoutConstraint* passLoginTitleLabelTopConstraint = [NSLayoutConstraint constraintWithItem:passLoginTitleLabel attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:passLoginBtn attribute:NSLayoutAttributeTop multiplier:1.0f constant:15.0f];
    NSLayoutConstraint* passLoginTitleLabelLeftConstraint = [NSLayoutConstraint constraintWithItem:passLoginTitleLabel attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:passLoginBtn attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:-40.0f];
    //iOS 6.0或者7.0调用addConstraints
    //[self.view addConstraints:@[leftConstraint, rightConstraint, topConstraint, heightConstraint]];

    //iOS 8.0以后设置active属性值
    passLoginTitleLabelTopConstraint.active = YES;
    passLoginTitleLabelLeftConstraint.active = YES;

//    UILabel* passLoginTipLabel = [[UILabel alloc] initWithFrame:CGRectMake(
//                                                                         passLoginBtn.bounds.size.width * 0.4,
//                                                                         40.f,
//                                                                         passLoginBtn.bounds.size.width * 0.5,
//                                                                         15.f)];
    UILabel* passLoginTipLabel = [[UILabel alloc] initWithFrame: CGRectZero];
    passLoginTipLabel.translatesAutoresizingMaskIntoConstraints = NO;
    passLoginTipLabel.backgroundColor = [UIColor clearColor];
    passLoginTipLabel.text = @"原百度账号可直接登录";
    passLoginTipLabel.textColor = [UIColor colorWithRed:16.0 / 255.0 green:140.0 / 255.0 blue:238.0 / 255.0 alpha:1];
    passLoginTipLabel.font = [UIFont systemFontOfSize:13.f];
    [passLoginBtn addSubview:passLoginTipLabel];

    NSLayoutConstraint* passLoginTipLabelTopConstraint = [NSLayoutConstraint constraintWithItem:passLoginTipLabel attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:passLoginBtn attribute:NSLayoutAttributeTop multiplier:1.0f constant:40.0f];
    NSLayoutConstraint* passLoginTipLabelLeftConstraint = [NSLayoutConstraint constraintWithItem:passLoginTipLabel attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:passLoginBtn attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:-40.0f];
    //iOS 6.0或者7.0调用addConstraints
    //[self.view addConstraints:@[leftConstraint, rightConstraint, topConstraint, heightConstraint]];

    //iOS 8.0以后设置active属性值
    passLoginTipLabelTopConstraint.active = YES;
    passLoginTipLabelLeftConstraint.active = YES;

    UIImage* PassLoginImage = [UIImage imageNamed:@"PassLoginIcon"];
    UIImageView* PassLoginIconIcon = [[UIImageView alloc] initWithFrame:CGRectMake(
                                                                                 passLoginBtn.bounds.size.width * 0.2,
                                                                                 passLoginBtn.bounds.size.height * 0.2,
                                                                                 (passLoginBtn.bounds.size.height * 0.6) / PassLoginImage.size.height * PassLoginImage.size.width,
                                                                                 passLoginBtn.bounds.size.height * 0.6)];
    PassLoginIconIcon.image = PassLoginImage;
    [passLoginBtn addSubview:PassLoginIconIcon];

    [viewController.view addSubview:passLoginBtn];
    [passLoginBtn addTarget:self action:@selector(_showPassLoginView) forControlEvents:UIControlEventTouchDown];


    if (self.viewController.navigationController == NULL) {
        UINavigationController *nav = [[UINavigationController alloc] init];

        self.webView.window.rootViewController = nav;
        [nav pushViewController:self.viewController animated:false];
//        [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
    }

    [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];

    [self.viewController.navigationController pushViewController:viewController animated:true];

    callbackId = command.callbackId;
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"loginSuccess" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(loginSuccessCallback) name:@"loginSuccess" object:nil];
}

- (void)showPassLoginView:(CDVInvokedUrlCommand*)command
{
    [self _showPassLoginView];

    callbackId = command.callbackId;

    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"loginSuccess" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(loginSuccessCallback) name:@"loginSuccess" object:nil];
}

- (void)_showPassLoginView
{
    PASSLoginViewController *loginVC = [[PASSLoginViewController alloc] init];
    loginVC.hidesBottomBarWhenPushed = YES;

    if (self.viewController.navigationController == NULL) {
        UINavigationController *nav = [[UINavigationController alloc] init];

        self.webView.window.rootViewController = nav;
        [nav pushViewController:self.viewController animated:NO];
        [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
    }

    [self.viewController.navigationController pushViewController:loginVC animated:YES];
}

- (void)showUCLoginView:(CDVInvokedUrlCommand*)command
{
    [self _showUCLoginView];

    callbackId = command.callbackId;

    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"loginSuccess" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(loginSuccessCallback) name:@"loginSuccess" object:nil];
}

- (void)_showUCLoginView
{
//    TODO 在config.xml中配置登陆页地址和注册页地址
    NSString* moduleName = @"https://login.bce.baidu.com";
    NSString* moduleTitle = @"云账号登录";
    NSString* moduleConfigFile = @"www/uc-reg/config.xml";

    ViewController *viewController = [[ViewController alloc] init];

    viewController.startPage = moduleName;
    viewController.configFile = moduleConfigFile;
    viewController.title = moduleTitle;

    [[UINavigationBar appearance] setTranslucent:NO];
    [[UINavigationBar appearance] setBarTintColor:[UIColor colorWithRed:25.0 / 255.0 green:35.0 / 255.0 blue:60.0 / 255.0 alpha:1]];

    UIColor *titleColor = [UIColor colorWithRed:255.0 / 255.0 green:255.0 / 255.0 blue:255.0 / 255.0 alpha:1];
    [[UINavigationBar appearance] setTitleTextAttributes:@{
                                                            NSForegroundColorAttributeName: titleColor
                                                            }];
    viewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"BackIcon"] style:UIBarButtonItemStylePlain target:self action:@selector(popView)];
    [viewController.navigationItem.leftBarButtonItem setTintColor:[UIColor whiteColor]];

    viewController.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"注册" style:UIBarButtonItemStylePlain target:self action:@selector(showUCRegisterPage)];
    [viewController.navigationItem.rightBarButtonItem setTintColor:[UIColor whiteColor]];

    if (self.viewController.navigationController == NULL) {
        UINavigationController *nav = [[UINavigationController alloc] init];

        self.webView.window.rootViewController = nav;
        [nav pushViewController:self.viewController animated:NO];
        [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
    }

    [self.viewController.navigationController pushViewController:viewController animated:YES];
}

- (NSString*) getCookieOf:(NSURL *)url
{
    NSString *hostUrl = [NSString stringWithFormat:@"%@://%@", url.scheme, url.host];
    __block NSString* cookieStr = @"";

    if (hostUrl != nil) {
        NSArray* cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:[NSURL URLWithString:hostUrl]];

        [cookies enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            NSHTTPCookie *cookie = obj;

            cookieStr = [cookieStr stringByAppendingFormat:@"%@=%@; ",cookie.name,cookie.value];
        }];
    }

    return cookieStr;
}

- (void) ucLoginSuccess:(CDVInvokedUrlCommand*)command
{
    NSURL * postLogin = [NSURL URLWithString:@"https://login.bce.baidu.com/postlogin?_1495851167415&redirect=http%3A%2F%2Fconsole.bce.baidu.com"];

    NSString* cookieStr = [self getCookieOf:postLogin];

    cookieStr = [cookieStr stringByAppendingString:@"bce-login-type=UC;"];

    [self sendRequest:postLogin withCookie:cookieStr];
}

- (void) sendRequest:(NSURL*) url withCookie:(NSString*)cookie
{
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    manager.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeNone];

    [manager.requestSerializer setValue:cookie forHTTPHeaderField:@"Cookie"];

    manager.responseSerializer = [TextResponseSerializer serializer];

    [manager setTaskWillPerformHTTPRedirectionBlock:^NSURLRequest *(NSURLSession *session, NSURLSessionTask *task, NSURLResponse *response, NSURLRequest *request) {
        NSURL *reqURL = request.URL;

        NSString *cookieStr = [self getCookieOf:reqURL];

        [self sendRequest:reqURL withCookie:cookieStr];

        return nil;
    }];

    [manager GET:url.absoluteString parameters:nil progress:nil success:^(NSURLSessionTask *task, id responseObject) {
        [[NSNotificationCenter defaultCenter] postNotificationName:@"loginSuccess" object:nil];
    } failure:nil];
}

- (void) showUCRegisterPage
{
    ViewController *viewController = [[ViewController alloc] init];

    viewController.showNavigationBar = true;
    viewController.startPage = @"/uc-reg/index.html";
    viewController.configFile = @"www/uc-reg/config.xml";
    viewController.title = @"注册";

    [[UINavigationBar appearance] setTranslucent:NO];
    [[UINavigationBar appearance] setBarTintColor:[UIColor colorWithRed:25.0 / 255.0 green:35.0 / 255.0 blue:60.0 / 255.0 alpha:1]];

    UIColor *titleColor = [UIColor colorWithRed:255.0 / 255.0 green:255.0 / 255.0 blue:255.0 / 255.0 alpha:1];
    [[UINavigationBar appearance] setTitleTextAttributes:@{
                                                           NSForegroundColorAttributeName: titleColor
                                                           }];
    viewController.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"BackIcon"] style:UIBarButtonItemStylePlain target:self action:@selector(popView)];
    [viewController.navigationItem.leftBarButtonItem setTintColor:[UIColor whiteColor]];

    [self.viewController.navigationController pushViewController:viewController animated:true];
}

- (void)popView:(CDVInvokedUrlCommand*)command
{
    [self popView];
}

- (void)popView
{
    [self.viewController.navigationController popViewControllerAnimated:YES];

    // 先影藏导航栏，上一个 viewcontroller 在 viewWillAppear 是会自己判断是否显示
    [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
}

- (void)goToRoot
{
    [self.viewController.navigationController popToRootViewControllerAnimated:NO];
    [self.viewController.navigationController setNavigationBarHidden:YES animated:NO];
}

- (void)loginSuccessCallback
{
    [self goToRoot];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
//    NSString* js = [NSString stringWithFormat:@"cordova.require('cordova/exec').nativeCallback('%@',%d,%@,%d, %d)", callbackId, YES, @"''", NO, YES];
//    [self.webViewEngine evaluateJavaScript:js completionHandler:nil];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"loginSuccess" object:nil];
}

- (void)logout:(CDVInvokedUrlCommand*)command
{
//    SAPILoginModel *model = [SAPIMainManager sharedManager].currentLoginModel;
//    [[SAPIMainManager sharedManager].loginService logout:model];

    [self postBceLogout:command];
}

- (void)postBceLogout:(CDVInvokedUrlCommand*)command {
    AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
    manager.securityPolicy = [AFSecurityPolicy policyWithPinningMode:AFSSLPinningModeNone];

    manager.requestSerializer = [AFJSONRequestSerializer serializer];
    manager.responseSerializer = [TextResponseSerializer serializer];
    [manager GET:@"https://login.bcetest.baidu.com/logout" parameters:nil progress:nil
         success:^(NSURLSessionTask *task, id responseObject) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } failure:nil];
}


@end



@implementation LoginViewController

- (void)viewWillAppear:(BOOL)animated
{
    [self.navigationController setNavigationBarHidden:YES animated:NO];
}

@end
