package gov.samhsa.bhits.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class SpringBootAppRunnerApplication implements BeanPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAppRunnerApplication.class, args);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RequestMappingHandlerMapping) {
            RequestMappingHandlerMapping requestMappingHandlerMapping = ((RequestMappingHandlerMapping) bean);
            logger.info("Setting 'RemoveSemicolonContent' on 'RequestMappingHandlerMapping'-bean to false. Bean name: {}", beanName);
            requestMappingHandlerMapping.setRemoveSemicolonContent(false);
            logger.info("Setting 'UseSuffixPatternMatch' on 'RequestMappingHandlerMapping'-bean to false. Bean name: {}", beanName);
            requestMappingHandlerMapping.setUseSuffixPatternMatch(false);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
