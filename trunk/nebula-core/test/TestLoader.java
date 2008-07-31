import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;

import org.nebulaframework.util.io.IOSupport;


public class TestLoader extends SecureClassLoader {

	public TestLoader() {
		super();
	}

	public TestLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		
		if (name.equals("ExitInvoker")) {
			System.out.println("ExitInvoker");
			try {
				InputStream is = new FileInputStream("C:\\ExitInvoker.class");
				
				byte[] b = IOSupport.readBytes(is);
				
				URL url = new URL("http://blah/");
				CodeSource cs = new CodeSource(url, (Certificate[]) null);
				
				return defineClass(name, b, 0, b.length,cs);
				
			} catch (Exception e) {
				throw new ClassNotFoundException("Exp");
			}
		}
		else {
			System.err.println("WTH " + name);
		}
		
		if (name.startsWith("java")) {
			return super.loadClass(name);
		}
		// Build the physical class name
		String resName = "/" + name.replaceAll("\\.", "/") + ".class";
		
		// Attempt to get the input stream for the class file
		InputStream is = Class.forName(name).getResourceAsStream(resName);
		
		byte[] bytes = null;
		
		try {
			bytes = IOSupport.readBytes(is);
		} catch (IOException e) {
			throw new ClassNotFoundException("IOException");
		}
		
		URL url = null;
		try {
			url = new URL("http://blah/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		CodeSource cs = new CodeSource(url, (Certificate[]) null);
		Permissions permissions = new Permissions();
		ProtectionDomain domain = new  ProtectionDomain(cs, permissions);
		
		return defineClass(name, bytes, 0, bytes.length, domain);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}


	
	
}
