package eu.europeana.api.iiif.exceptions;

import eu.europeana.api.commons_sb3.error.EuropeanaGlobalExceptionHandler;
import eu.europeana.api.commons_sb3.error.i18n.I18nService;
import eu.europeana.api.commons_sb3.error.i18n.I18nServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler that catches all errors and logs the interesting ones
 *
 * @author Srishti Singh
 * Created on 20-11-2024
 */
@RestControllerAdvice
class GlobalExceptionHandler extends EuropeanaGlobalExceptionHandler {

    //
//     <!-- configurable error messages -->
//    <beans:bean id="set_i18n_messageSource" class="org.springframework.context.support.ReloadableResourceBundleMessageSource">
//		<beans:property name="basename" value="classpath:messages"/>
//		<beans:property name="defaultEncoding" value="utf-8"/>
//    </beans:bean>
//    <beans:bean id="i18nService" class="eu.europeana.api.commons.config.i18n.I18nServiceImpl">
//    	<beans:property name="messageSource" ref="set_i18n_messageSource"/>
//    </beans:bean>

    @Bean(name = "i18nService")
    public I18nService getI18nService() {
        I18nServiceImpl service =  new I18nServiceImpl();
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("common_messages", "messages");
        messageSource.setDefaultEncoding("utf-8");
        service.setMessageSource(messageSource);
        System.out.println("created bean in iiiif " + service);
        return service;
    }
}
