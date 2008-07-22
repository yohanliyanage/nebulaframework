import java.util.UUID;

public class Test {
	public static void main(String[] args) {
		
		for (int i=0; i< 10; i++) {
			UUID id = UUID.randomUUID();
			System.out.println(id.toString().getBytes().length);
		}
	}
}
