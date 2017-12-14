package com.xiaobiao.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author wuxiaobiao
 * @create 2017-12-07 12:00
 * To change this template use File | Settings | Editor | File and Code Templates.
 **/
@Setter
@Getter
@ToString
public class ActiveUser implements Serializable {
    private static final long serialVersionUID = 5072485324928961393L;

    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户code码
     */
    private String userCode;
    /**
     * 用户名
     */
    private String userName;

}

