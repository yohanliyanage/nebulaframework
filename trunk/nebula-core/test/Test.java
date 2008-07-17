import java.net.InetAddress;

public class Test {
	public static void main(String[] args) throws Exception {
//		InetAddress[] arr = (InetAddress.getAllByName("localhost"));
//		for (InetAddress a : arr) {
//			System.out.println(a.getHostAddress());
//		}
		System.out.println(InetAddress.getLocalHost().getHostAddress());
	}
}
