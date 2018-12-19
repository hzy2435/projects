package com.itlaoqi.bsbdj.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;

@Configuration
public class DruidConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource druid() {

		DruidDataSource ds = new DruidDataSource();
		return ds;

	}

	@Bean
	public ServletRegistrationBean<StatViewServlet> statViewServlet() {

		ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet(),
				"/druid/*");
		Map<String, String> params = new HashMap<>();
		params.put("loginUsername", "admin");
		params.put("loginPassword", "123456");
		params.put("allow", "");
		params.put("deny", "33.31.51.88");
		bean.setInitParameters(params);
		return bean;

	}
	
	@Bean
	public FilterRegistrationBean<WebStatFilter> webStatFilter() {
		
		FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(new WebStatFilter());
		bean.addUrlPatterns("/*");
		Map<String, String> params = new HashMap<>();
		params.put("exclusions", "*.js,*.css,*.woff,*.png,/druid/*");
		bean.setInitParameters(params);
		
		return bean;
		
	}

}
