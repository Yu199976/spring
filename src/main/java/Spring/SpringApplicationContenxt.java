package Spring;


import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class SpringApplicationContenxt {
    private Class ConfigClass;
    //beanDefinitionMap
    ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //单例池singletonMap
    ConcurrentHashMap<String, Object> SingletonObjects = new ConcurrentHashMap<>();
    ArrayList<BeanPostProcessor> beanPostProcessorArrayList = new ArrayList<>();

    public SpringApplicationContenxt(Class configClass) throws URISyntaxException {
        ConfigClass = configClass;
        //判断当前类有没有ComponentScan这个注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            //获取ComponentScan注解对象
            ComponentScan annotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            //获取注解上的配置，例如：com.ypy.service
            String value = annotation.value();
            //获取target文件目录下的字节码文件
            ClassLoader classLoader = SpringApplicationContenxt.class.getClassLoader();
            //获取字节码文件
            String url = classLoader.getResource(value.replace(".", "/")).toURI().getPath();
            File file = new File(url);
            //判断当前路径是否是个文件夹
            if (file.isDirectory()) {
                File[] files = file.listFiles();

                for (File f : files) {
                    //判断这些文件后缀是否以.class结尾
                    String path = f.getAbsolutePath();
                    //判断是否是class文件
                    if (path.endsWith(".class")) {
                        //判断当前字节码文件是否有Component注解
                        Class<?> aClass = null;
                        try {
                            //通过拼接和截取,获取目录下的class文件的路径如com.ypy.UserService
                            String ClassPath = value + "." + f.getName().replace(".class", "");
                            //反射
                            aClass = Class.forName(ClassPath);
                            //判断是否包含@Component注解
                            if (aClass.isAnnotationPresent(Component.class)) {
                                if (BeanPostProcessor.class.isAssignableFrom(aClass)){
                                    try {
                                        BeanPostProcessor instance = (BeanPostProcessor) aClass.newInstance();
                                        beanPostProcessorArrayList.add(instance);

                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }

                                }

                                Component ComponentAnnotation = aClass.getAnnotation(Component.class);
                                String beanName = ComponentAnnotation.value();
                                //如果注解未填写，存入默认beanName
                                if("".equals(beanName)){
                                    beanName = Introspector.decapitalize(aClass.getSimpleName());
                                }

                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(aClass);
                                if (aClass.isAnnotationPresent(Scope.class)) {
                                    //将扫描到的Scope注解的属性存入beanDefinition对象
                                    Scope ScopeAnnotation = aClass.getAnnotation(Scope.class);
                                    beanDefinition.setScope(ScopeAnnotation.value());
                                } else {
                                    //默认是单例bean
                                    beanDefinition.setScope("singleton");
                                }

                                beanDefinitionMap.put(beanName, beanDefinition);
                                /**
                                 * 模拟实现bean注解
                                 */
                                Method[] methods = aClass.getMethods();
                                for (Method method : methods) {  //遍历方法
                                    if (method.isAnnotationPresent(Bean.class)) {  //判断成员属性上是否有bean注解
                                        beanDefinition.setType(method.getReturnType());  //方法的返回结果类型
                                        beanDefinition.setScope("singleton");   //单例
                                        Bean bean = method.getAnnotation(Bean.class);
                                        beanName = bean.value(); //获取注解的value
                                        if("".equals(beanName)){
                                            System.out.println(method.getName());
                                            beanName = method.getName();
                                        }
                                        beanDefinitionMap.put(beanName,beanDefinition) ;
                                    }
                                }
                            }

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        //变历beanDefinitionMap，将单例bean存在单例池SingletonObjects里
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton") || beanDefinition.getScope().equals("")) {
                //如果是单例bean对象,调用createBean方法创建bean对象,放入单例池中
                Object bean = this.createBean(beanName, beanDefinition);
                SingletonObjects.put(beanName, bean);
            }
        }
    }

    //创建bean对象
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        //通过使用
        Class clazz = beanDefinition.getType();
        Object bean = null;
        try {
            //调用无参构造函数创建bean对象
            bean = clazz.getConstructor().newInstance();

            //实现依赖注入
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                //当前对象有@Autowired注解
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(bean,getBean(field.getName()));
                }
            }
            //依赖注入后实现Aware接口的各种回调
            if(bean instanceof BeanNameAware){
                //如果实现了这个接口
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            //初始化前调用afterPropertiesSet()
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorArrayList) {
                bean = beanPostProcessor.postProcessBeforeInitialization(beanName, bean);
            }

            //初始化
            if(bean instanceof InitializingBean){
                //如果实现了这个接口
                ((InitializingBean) bean).afterPropertiesSet();
            }

            //BeanPost Process初始化后 AOP
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorArrayList) {
                bean = beanPostProcessor.postProcessAfterInitialization(beanName, bean);

            }


        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return bean;
    }


    //模拟Spring容器的getBean方法
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if (scope.equals("singleton") || scope.equals("")) {
                //直接从单例池中获取单例bean
                Object bean = SingletonObjects.get(beanName);
                if (bean == null) {
                    //创建bean
                    Object o = createBean(beanName, beanDefinition);
                    //存到单例池汇总
                    SingletonObjects.put(beanName, o);
                }
                return bean;
            } else {
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
