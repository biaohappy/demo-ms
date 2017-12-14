package com.xiaobiao.service;


import com.xiaobiao.exception.MsException;
import com.xiaobiao.manager.CoreUserInfoService;
import com.xiaobiao.param.UserParam;
import com.xiaobiao.result.UserResult;
import com.xiaobiao.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuxiaobiao
 * @create 2017-12-06 15:28
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
@Service
public class DemoService {

    @Autowired
    private CoreUserInfoService coreUserInfoService;

    public List<UserResult> queryUserList(UserParam userParam) {
        Response<List<UserResult>> resp = coreUserInfoService.queryUserList(userParam);
        if (!resp.isSuccess()) {
            throw new MsException(resp.getErrorCode(), resp.getErrorMsg());
        }
        return resp.getResult();
    }

}
