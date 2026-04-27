package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.*;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

public class ApplicationContext {

    private final String basePackage;
    private final Map<String, Object> beans = new HashMap<>();
    private final List<Class<?>> classes = new ArrayList<>();

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> beanClasses = new HashSet<>();

        beanClasses.addAll(reflections.getTypesAnnotatedWith(Component.class));
        beanClasses.addAll(reflections.getTypesAnnotatedWith(Configuration.class));
        beanClasses.addAll(reflections.getTypesAnnotatedWith(Repository.class));
        beanClasses.addAll(reflections.getTypesAnnotatedWith(Service.class));
        beanClasses.addAll(reflections.getTypesAnnotatedWith(Bean.class));

        classes.addAll(beanClasses);
    }

    public <T> T genBean(String beanName) {
        if(beans.containsKey(beanName))
        {
            return (T) beans.get(beanName);
        }

        for(Class<?> clazz: classes)
        {
            String currentBeanName = lowerFirst(clazz.getSimpleName());
            if(currentBeanName.equals(beanName))
            {
                Object instance = createInstance(clazz);
                beans.put(beanName, instance);
                return (T) instance;
            }
        }

        return null;
    }

    private Object createInstance(Class<?> clazz) {
        try{
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];

            for(int i = 0; i<args.length; i++){
                args[i] = getBeanByType(parameterTypes[i]);
            }
            return constructor.newInstance(args);
        }catch (Exception e){
            throw new RuntimeException("Failed to create : "+ clazz.getName(),e);
        }
    }

    private Object getBeanByType(Class<?> parameterType) {
        for(Object bean : beans.values())
        {
            if(parameterType.isInstance(bean)) return bean;
        }

        for(Class<?> clazz : classes)
        {
            if(parameterType.isAssignableFrom(clazz))
            {
                String beanName = lowerFirst(clazz.getSimpleName());
                return genBean(beanName);
            }
        }

        return null;
    }

    private String lowerFirst(String simpleName) {
        return Character.toLowerCase(simpleName.charAt(0) ) + simpleName.substring(1);
    }
}
