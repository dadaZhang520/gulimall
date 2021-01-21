package com.dadazhang.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.dadazhang.common.valid.AddGroup;
import com.dadazhang.common.valid.ListValue;
import com.dadazhang.common.valid.UpdateGroup;
import com.dadazhang.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;


/**
 * 品牌
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Null(message = "新增品牌的时候brandId必须为空", groups = AddGroup.class)
    @NotNull(message = "修改品牌的时候brandId必须不为空", groups = {UpdateGroup.class,UpdateStatusGroup.class})
    @TableId
    private Long brandId;

    @NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
    private String name;

    @NotEmpty(message = "品牌logo必须提交", groups = AddGroup.class)
    //url校验器
    @URL(message = "logo必须是一个合法的地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;

    @NotBlank(message = "品牌描述必须提交", groups = AddGroup.class)
    private String descript;

    //自定义校验器
    @NotNull(message = "品牌显示状态不能为空", groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(values = {0, 1}, groups = {AddGroup.class, UpdateGroup.class , UpdateStatusGroup.class})
   // @TableLogic(value = "1", delval = "0")
    private Integer showStatus;

    @NotEmpty(message = "品牌检索首字母必须提交", groups = AddGroup.class)
    //使用正则表达式校验器
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个合法字母", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;

    @NotNull(message = "品牌排序必须提交", groups = AddGroup.class)
    @Min(value = 0, message = "排序必须大于等于零", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
