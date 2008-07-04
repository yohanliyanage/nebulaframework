package org.nebulaframework.core.job.archive;

import java.io.Serializable;

import org.nebulaframework.util.hashing.SHA1Generator;
import org.springframework.util.Assert;

public class GridArchive implements Serializable {

	private static final long serialVersionUID = 3354082308238249555L;
	
	private String[] jobClassNames;
	private byte[] bytes;
	private String hash;
	
	public GridArchive(byte[] bytes, String[] jobClassNames) {
		super();
		
		// Assertions
		Assert.notNull(bytes);
		Assert.notNull(jobClassNames);
		
		this.bytes = bytes;
		this.jobClassNames = jobClassNames;
		
		// Generate SHA1 Hash for bytes
		hash = SHA1Generator.bytesToString(SHA1Generator.generate(bytes));
	}

	public byte[] getBytes() {
		return bytes;
	}

	
	public String getHash() {
		return hash;
	}

	public String[] getJobClassNames() {
		return jobClassNames;
	}

	
}
