package com.dadazhang.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
@Data
public class MemberVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long levelId;

    private String username;

    private String password;

    private String nickname;

    private String mobile;

    private String email;

    private String header;

    private Integer gender;

    private Date birth;

    private String city;

    private String job;

    private String sign;

    private Integer sourceType;

    private Integer integration;

    private Integer growth;

    private Integer status;

    private Date createTime;

    private String socialUid;

    private String accessToken;

    private long expiresIn;

}
