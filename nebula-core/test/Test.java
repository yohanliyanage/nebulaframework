
public class Test {
	public static void main(String[] args) {

		System.setSecurityManager(new SecurityManager());

		System.out.println("Im Running");
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					
					Class<?> c = Thread.currentThread().getContextClassLoader()
							.loadClass("ExitInvoker");
					
					System.out.println("Exiting");
					c.newInstance().toString();


					Thread.sleep(2000);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});

		t.setContextClassLoader(new TestLoader());
		t.start();
//
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				while (true) {
//					try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//
//		}).start();
	}
}
