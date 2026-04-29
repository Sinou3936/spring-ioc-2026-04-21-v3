package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.*;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;

public class ApplicationContext {

    private final String basePackage;
    private final Map<String, Object> beans = new HashMap<>();
    private final List<Class<?>> classes = new ArrayList<>();
    private final Map<String, Method> beanMethods = new HashMap<>();
    private final Map<String,Object> configInstance = new HashMap<>();

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

        for(Class<?> configClass : reflections.getTypesAnnotatedWith(Configuration.class))
        {
            for(Method method : configClass.getDeclaredMethods())
            {
                if(method.isAnnotationPresent(Bean.class))
                    beanMethods.put(method.getName(),method);
            }
        }
    }

    @SneakyThrows
    public <T> T genBean(String beanName) {
        if(beans.containsKey(beanName))
        {
            return (T) beans.get(beanName);
        }
        else if(beanMethods.containsKey(beanName))
        {
            Method method = beanMethods.get(beanName);
            Object objectInstance = getOrCreateConfigInstance(method.getDeclaringClass());

            Object[] args = resolverArgs(method);
            Object bean = method.invoke(objectInstance, args);
            beans.put(beanName, bean);
            return (T) bean;
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

    private Object[] resolverArgs(Method method) {
        try{
            Object[] args = resolverParamsArgs(method.getParameterTypes());
            return args;
        }catch (Exception e){
            throw new RuntimeException("Failed to create : "+ method.getName(),e);
        }

    }

    private Object getOrCreateConfigInstance(Class<?> declaringClass) {
        String className = declaringClass.getName();
        if(configInstance.containsKey(className))
        {
            return configInstance.get(className);
        }

        try{
            Object instance = declaringClass.getDeclaredConstructor().newInstance();
            configInstance.put(className,instance);
            return instance;
        }catch (Exception e){
            throw new RuntimeException("Failed to create config instance : "+ declaringClass.getName(), e);
        }
    }

    private Object createInstance(Class<?> clazz) {
        try{
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            Object[] args = resolverParamsArgs(constructor.getParameterTypes());
            return constructor.newInstance(args);
        }catch (Exception e){
            throw new RuntimeException("Failed to create : "+ clazz.getName(),e);
        }
    }

    private Object[] resolverParamsArgs(Class<?>[] paramsTypes) {
        Object[] args = new Object[paramsTypes.length];

        for(int i = 0; i<args.length; i++){
            args[i] = getBeanByType(paramsTypes[i]);
        }
        return args;
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
