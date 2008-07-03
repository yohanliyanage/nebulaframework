package org.nebulaframework.deployment.classloading.node.exporter;

public interface GridNodeClassExporter {
	public byte[] exportClass(String name) throws ClassNotFoundException;
}
