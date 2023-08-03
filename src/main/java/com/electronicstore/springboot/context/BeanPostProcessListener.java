package com.electronicstore.springboot.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class BeanPostProcessListener implements BeanPostProcessor {

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getPackageName().equals("com.electronicstore.springboot.service")) {
            System.out.println("bean " + beanName + " after initialization:");
            System.out.println("   name=" + beanName);
            System.out.println("   package=" + bean.getClass().getPackageName());
            System.out.println("   class=" + bean.getClass().getSimpleName());
            for (Field field : bean.getClass().getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object fieldInstance = field.get(bean);
                    System.out.println("   field=" + field.getName());
                    System.out.println("         package=" + fieldInstance.getClass().getPackageName());
                    System.out.println("         class=" + fieldInstance.getClass().getSimpleName());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

}
