package com.dadazhang.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 秒杀活动场次
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:22:10
 */
@Data
@TableName("sms_seckill_session")
public class SeckillSessionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    private String name;

    private Date startTime;

    private Date endTime;

    private Integer status;

    private Date createTime;

    @TableField(exist = false)
    private List<SeckillSkuRelationEntity> seckillSkuRelations;

}
