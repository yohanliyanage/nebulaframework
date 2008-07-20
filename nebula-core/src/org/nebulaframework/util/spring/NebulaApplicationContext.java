/*
 * Copyright (C) 2008 Yohan Liyanage. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.nebulaframework.util.spring;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Custom implementation of Spring Framework's
 * {@code ClassPathXmlApplicationContext}, which disables the XSD validation to
 * improve the start up time of application context. Furthermore, this allows to
 * specify a Property File directly which can be used to configure the
 * {@code ApplicationContext}.
 * <p>
 * XSD validation issue was referred from the <a
 * href='http://jira.springframework.org/browse/SPR-3894'> Spring JIRA Issue
 * Tracker</a>, as stated by Taras Tielkes.
 * 
 * @author Yohan Liyanage, [Ref : Taras Tielkes]
 * 
 */
public class NebulaApplicationContext extends ClassPathXmlApplicationContext {

	/**
	 * Initialize the bean definition reader used for loading the bean
	 * definitions of this context, and disabled XSD Validation to speed up the
	 * context start up time.
	 */
	@Override
	protected void initBeanDefinitionReader(
			XmlBeanDefinitionReader beanDefinitionReader) {
		beanDefinitionReader
				.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
		beanDefinitionReader.setNamespaceAware(true);
	}

	/**
	 * Create a new NebulaApplicationContext for bean-style configuration.
	 * 
	 * @see #setConfigLocation
	 * @see #setConfigLocations
	 * @see #afterPropertiesSet()
	 */
	public NebulaApplicationContext() {
		super();
	}

	/**
	 * Create a new NebulaApplicationContext for bean-style configuration.
	 * 
	 * @param parent
	 *            the parent context
	 * @see #setConfigLocation
	 * @see #setConfigLocations
	 * @see #afterPropertiesSet()
	 */
	public NebulaApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	/**
	 * Create a new NebulaApplicationContext, loading the definitions from the
	 * given XML file and automatically refreshing the context.
	 * <p>
	 * This is a convenience method to load class path resources relative to a
	 * given Class. For full flexibility, consider using a
	 * GenericApplicationContext with an XmlBeanDefinitionReader and a
	 * ClassPathResource argument.
	 * 
	 * @param path
	 *            relative (or absolute) path within the class path
	 * @param clazz
	 *            the class to load resources with (basis for the given paths)
	 * @throws BeansException
	 *             if context creation failed
	 * @see org.springframework.core.io.ClassPathResource#ClassPathResource(String,
	 *      Class)
	 * @see org.springframework.context.support.GenericApplicationContext
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	public NebulaApplicationContext(String path, Class<?> clazz)
			throws BeansException {
		super(path, clazz);
	}

	/**
	 * Create a new NebulaApplicationContext, loading the definitions from the
	 * given XML file and automatically refreshing the context.
	 * 
	 * @param configLocation
	 *            resource location
	 * @throws BeansException
	 *             if context creation failed
	 */
	public NebulaApplicationContext(String configLocation)
			throws BeansException {
		super(configLocation);
	}

	/**
	 * Create a new NebulaApplicationContext with the given parent, loading the
	 * definitions from the given XML files and automatically refreshing the
	 * context.
	 * 
	 * @param configLocations
	 *            array of resource locations
	 * @param parent
	 *            the parent context
	 * @throws BeansException
	 *             if context creation failed
	 */
	public NebulaApplicationContext(String[] configLocations,
			ApplicationContext parent) throws BeansException {
		super(configLocations, parent);
	}

	/**
	 * Create a new NebulaApplicationContext with the given parent, loading the
	 * definitions from the given XML files.
	 * 
	 * @param configLocations
	 *            array of resource locations
	 * @param refresh
	 *            whether to automatically refresh the context, loading all bean
	 *            definitions and creating all singletons. Alternatively, call
	 *            refresh manually after further configuring the context.
	 * @param parent
	 *            the parent context
	 * @throws BeansException
	 *             if context creation failed
	 * @see #refresh()
	 */
	public NebulaApplicationContext(String[] configLocations, boolean refresh,
			ApplicationContext parent) throws BeansException {
		super(configLocations, refresh, parent);
	}

	/**
	 * Create a new NebulaApplicationContext, loading the definitions from the
	 * given XML files.
	 * 
	 * @param configLocations
	 *            array of resource locations
	 * @param refresh
	 *            whether to automatically refresh the context, loading all bean
	 *            definitions and creating all singletons. Alternatively, call
	 *            refresh manually after further configuring the context.
	 * @throws BeansException
	 *             if context creation failed
	 * @see #refresh()
	 */
	public NebulaApplicationContext(String[] configLocations, boolean refresh)
			throws BeansException {
		super(configLocations, refresh);
	}

	/**
	 * Create a new NebulaApplicationContext with the given parent, loading the
	 * definitions from the given XML files and automatically refreshing the
	 * context.
	 * 
	 * @param paths
	 *            array of relative (or absolute) paths within the class path
	 * @param clazz
	 *            the class to load resources with (basis for the given paths)
	 * @param parent
	 *            the parent context
	 * @throws BeansException
	 *             if context creation failed
	 * @see org.springframework.core.io.ClassPathResource#ClassPathResource(String,
	 *      Class)
	 * @see org.springframework.context.support.GenericApplicationContext
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	public NebulaApplicationContext(String[] paths, Class<?> clazz,
			ApplicationContext parent) throws BeansException {
		super(paths, clazz, parent);
	}

	/**
	 * Create a new NebulaApplicationContext, loading the definitions from the
	 * given XML files and automatically refreshing the context.
	 * 
	 * @param paths
	 *            array of relative (or absolute) paths within the class path
	 * @param clazz
	 *            the class to load resources with (basis for the given paths)
	 * @throws BeansException
	 *             if context creation failed
	 * @see org.springframework.core.io.ClassPathResource#ClassPathResource(String,
	 *      Class)
	 * @see org.springframework.context.support.GenericApplicationContext
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	public NebulaApplicationContext(String[] paths, Class<?> clazz)
			throws BeansException {
		super(paths, clazz);
	}

	/**
	 * Create a new NebulaApplicationContext, loading the definitions from the
	 * given XML files and automatically refreshing the context.
	 * 
	 * @param configLocations
	 *            array of resource locations
	 * @throws BeansException
	 *             if context creation failed
	 */
	public NebulaApplicationContext(String[] configLocations)
			throws BeansException {
		super(configLocations);
	}

	/**
	 * Create a new NebulaApplicationContext using the XML file given and
	 * configures the context using the specified {@code Properties}.
	 * 
	 * @param configLocation
	 *            resource location
	 * @param props
	 * @throws BeansException
	 *             if context creation failed
	 */
	public NebulaApplicationContext(String configLocation, Properties props)
			throws BeansException {
		this();

		PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
		configurer.setProperties(props);

		this.addBeanFactoryPostProcessor(configurer);
		this.setConfigLocation(configLocation);
		this.refresh();
		this.start();
	}

	/**
	 * Create a new NebulaApplicationContext using the XML file given and
	 * configures the context using the specified Property File.
	 * 
	 * @param configLocation
	 *            resource location
	 * @param propsLocation
	 *            property file location
	 * @throws BeansException
	 *             if context creation failed
	 */
	public NebulaApplicationContext(String configLocation, String propsLocation)
			throws BeansException, IOException {

		// Load Properties
		Properties props = new Properties();
		FileInputStream fin = new FileInputStream(propsLocation);
		props.load(fin);
		fin.close();
		
		// Create Context
		PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
		configurer.setProperties(props);

		this.addBeanFactoryPostProcessor(configurer);
		this.setConfigLocation(configLocation);
		this.refresh();
		this.start();
	}

}
