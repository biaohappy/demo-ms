package com.xiaobiao.component;

import com.xiaobiao.model.ActiveUser;
import com.xiaobiao.param.UserParam;
import com.xiaobiao.result.UserResult;
import com.xiaobiao.service.DemoService;
import com.xiaobiao.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wuxiaobiao
 * @create 2017-12-07 11:13
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
@Slf4j
@Component
public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private DemoService demoService;


    //支持什么类型的token
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    @Override
    public String getName() {
        return "shirorealm";
    }

    //清除缓存
    @Override
    protected void doClearCache(PrincipalCollection principals) {
        super.doClearCache(principals);
    }

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();

        return simpleAuthorizationInfo;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //todo 从token中取出用户名
        String userCode = (String) token.getPrincipal();
        // todo 根据用户输入的userCode从数据库查询用户信息
        UserParam userParam = new UserParam();
        userParam.setUsercode(userCode);
        userParam.setIsNullError(false);
        List<UserResult> userInfoList = demoService.queryUserList(userParam);
        if (ObjectUtils.isNullOrEmpty(userInfoList)) {
            log.error("failed to doGetAuthenticationInfo, PARAMETER:{}", token);
            throw new UnknownAccountException();
        }
        UserResult userInfo = userInfoList.get(0);
        // 构建用户身份信息
        ActiveUser activeUser = new ActiveUser();
        activeUser.setUserId(userInfo.getId());
        activeUser.setUserCode(userInfo.getUsercode());
        activeUser.setUserName(userInfo.getUsername());
        //todo 返回认证信息由父类AuthenticatingRealm进行认证
        SimpleAuthenticationInfo simpleAuthenticationInfo = new SimpleAuthenticationInfo(
                activeUser, userInfo.getPassword(), ByteSource.Util.bytes(userInfo.getSalt()), getName());
        return simpleAuthenticationInfo;
    }

}
