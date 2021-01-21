package com.dadazhang.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    Set<Integer> set = new HashSet<>();

    //初始化的校验
    @Override
    public void initialize(ListValue constraintAnnotation) {
        //获取定义的校验值
        int[] values = constraintAnnotation.values();
        for (int value : values) {
            set.add(value);
        }
    }

    //进行校验
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }
}
