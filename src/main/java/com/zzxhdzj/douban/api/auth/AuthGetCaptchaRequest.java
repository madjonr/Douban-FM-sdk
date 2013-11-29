package com.zzxhdzj.douban.api.auth;

import com.zzxhdzj.douban.Constants;
import com.zzxhdzj.http.ApiRequest;
import com.zzxhdzj.http.TextApiResponse;
import org.apache.http.Header;

/**
 * Created with IntelliJ IDEA.
 * User: yangning.roy
 * Date: 11/24/13
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuthGetCaptchaRequest extends ApiRequest<TextApiResponse> {
    @Override
    public String getUrlString() {
        return Constants.CAPTCHA_ID;
    }

    @Override
    public TextApiResponse createResponse(int statusCode,Header[] headers) {
        return new TextApiResponse(statusCode);
    }
}