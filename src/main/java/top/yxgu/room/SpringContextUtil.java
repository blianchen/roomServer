package top.yxgu.room;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component  
public class SpringContextUtil implements ApplicationContextAware {  
  
	public static ApplicationContext context; // Spring应用上下文环境  
  
    /* 
     * 
     * 实现了ApplicationContextAware 接口，必须实现该方法； 
     * 通过传递applicationContext参数初始化成员变量applicationContext 
     */  
    public void setApplicationContext(ApplicationContext applicationContext)  
            throws BeansException {  
        SpringContextUtil.context = applicationContext;  
    }  
  
    public static ApplicationContext getApplicationContext() {  
        return context;  
    }  
  
    @SuppressWarnings("unchecked")  
    public static <T> T getBean(String name) throws BeansException {  
        return (T) context.getBean(name);
    }  
  
}  
