package org.nebulaframework.core.grid.cluster.manager;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;


public class TestManagerRunner {
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/manager/cluster-manager.xml");
	}
}
