package com.xiaobiao.controller;

import com.google.common.base.Throwables;
import com.xiaobiao.exception.DemoErrorCode;
import com.xiaobiao.exception.MsException;
import com.xiaobiao.model.ActiveUser;
import com.xiaobiao.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author wuxiaobiao
 * @create 2017-12-07 11:17
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
@Controller
@Slf4j
@RequestMapping("/")
public class LoginController {

    @Autowired
    private DemoService demoService;

    /**
     * 散列次数
     */
    //若配置文件中无count属性，则给一个默认值1
    @Value("${count:1}")
    private String count;

    /**
     * 盐
     */
    @Value("${salt}")
    private String salt;


    /**
     * 登录界面
     * @return
     */
    @RequestMapping("login")
    public String login(){
        return "system/login";
    }

    /**
     * 执行登录
     * @param request
     * @return
     */
    @RequestMapping("signIn")
    public String signIn(HttpServletRequest request){
        UsernamePasswordToken token = null;
        try {
            String userCode = request.getParameter("userCode");
            String password = request.getParameter("password");
//
//            //散列一次
//            Object paswordMd5 = new SimpleHash("MD5", password, salt, Integer.parseInt(count));
//            System.out.println(paswordMd5);

            request.setAttribute("userCode",userCode);
            token = new UsernamePasswordToken(userCode, password);
            Subject subject = SecurityUtils.getSubject();
            subject.login(token);
            log.info("userCode:"+userCode+",password:"+password);
            return "redirect:toMain";
        } catch (UnknownAccountException e) {
            request.setAttribute("errorMsg", DemoErrorCode.ERROR_CODE_341002.getErrorDesc());
            token.clear();
            log.error("failed to signIn, CAUSE:{}", Throwables.getStackTraceAsString(e));
        } catch (IncorrectCredentialsException e) {
            request.setAttribute("errorMsg", DemoErrorCode.ERROR_CODE_341003.getErrorDesc());
            token.clear();
            log.error("failed to signIn, CAUSE:{}", Throwables.getStackTraceAsString(e));
        } catch (AuthenticationException e) {
            request.setAttribute("errorMsg", DemoErrorCode.ERROR_CODE_341004.getErrorDesc());
            token.clear();
            log.error("failed to signIn, CAUSE:{}", Throwables.getStackTraceAsString(e));
        } catch (MsException e) {
            request.setAttribute("errorMsg", DemoErrorCode.ERROR_CODE_341005.getErrorDesc());
            token.clear();
            log.error("failed to signIn, CAUSE:{}", Throwables.getStackTraceAsString(e));
        } catch (Exception e) {
            request.setAttribute("errorMsg", DemoErrorCode.ERROR_CODE_341FFF.getErrorDesc());
            token.clear();
            log.error("failed to signIn, CAUSE:{}", Throwables.getStackTraceAsString(e));
        }
        return "system/login";
    }

    /**
     * 系统首页
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "toMain")
    public String first(Model model, HttpServletRequest request){
        try {
            //主体
            Subject subject = SecurityUtils.getSubject();
            //身份
            ActiveUser activeUser = (ActiveUser) subject.getPrincipal();
            model.addAttribute("activeUser", activeUser);
            HttpSession session = request.getSession();
            session.setAttribute("user",activeUser.getUserCode());
        }catch (MsException e){
            request.setAttribute("errorCode", e.getCode());
            request.setAttribute("errorMsg", e.getMessage());
            log.error("failed to signIn, CAUSE:{}", Throwables.getStackTraceAsString(e));
        }catch (Exception e){
            request.setAttribute("errorCode", DemoErrorCode.ERROR_CODE_341FFF.getErrorCode());
            request.setAttribute("errorMsg", DemoErrorCode.ERROR_CODE_341FFF.getErrorDesc());
            log.error("failed to toMain, CAUSE:{}", Throwables.getStackTraceAsString(e));
        }
        return "manager/index";
    }

    /**
     * 退出
     * @return
     */
    @RequestMapping("logout")
    public String logOut(){
        return "system/login";
    }
}
