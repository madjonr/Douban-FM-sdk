package com.zzxhdzj.douban.modules.channel;

import com.google.gson.annotations.SerializedName;
import com.zzxhdzj.douban.modules.Resp;

/**
 * Created with IntelliJ IDEA.
 * User: yangning.roy
 * Date: 12/9/13
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoginChannelsResp extends Resp{
    @SerializedName("data")
    public LoginChannelsData loginChannelsData;
}
