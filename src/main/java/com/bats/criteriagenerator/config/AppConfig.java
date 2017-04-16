package com.bats.criteriagenerator.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
@PropertySource ("classpath:criteriaBuilder-local.properties")
@ComponentScan ("com.bats.criteriagenerator")
public class AppConfig extends WebMvcConfigurerAdapter
{
	@Autowired
	Environment env;
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public CommonsMultipartResolver multipartResolver()
	{
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("utf-8");
		return resolver;
	}
	
	@Bean
	public Map<String, Class<?>> entityList() {
		Map<String, Class<?>> result = new HashMap<>();
		Reflections reflections = new Reflections("com.bats.criteriagenerator.entity");
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
		for (Class<?> clazz : annotated)
		{
			Entity entity = clazz.getAnnotation(Entity.class);
			result.put(entity.name(), clazz);
		}
		return result;
	}
	
	@Bean
	public String[] dateFormats() {
		String commaSeperatedDateformats = env.getProperty("queryprocessor.dateformats");
		return commaSeperatedDateformats.split(",");
	}
}
