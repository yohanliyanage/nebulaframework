package org.nebulaframework.core.grid.cluster.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.util.StopWatch;


public class TestManagerRunner {
	
	private static Log log = LogFactory.getLog(TestManagerRunner.class);
	
	public static void main(String[] args) {
		startContainer();
	}
	
	public static void startContainer() {
		log.info("ClusterManager Starting");
		StopWatch sw = new StopWatch();
		sw.start();
		new ClassPathXmlApplicationContext("org/nebulaframework/core/grid/cluster/manager/cluster-manager.xml");
		sw.stop();
		log.info("ClusterManager Started Up. [" + sw.getLastTaskTimeMillis() + " ms]");
	}
}