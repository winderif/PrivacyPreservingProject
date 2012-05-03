import client.ClientSystem;
import server.ServerSystem;
import java.util.LinkedList;
import java.util.Queue;
import util.Mode;

public class TaggingSystem {
	public static Queue<Object> mQueue;
	public static Mode mMode;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		mQueue = new LinkedList<Object>();
		mMode = new Mode(1);
		
		// C:\Zone\javaworkspace\Ballan\result\YouTube\Comedy\2			
		(new Thread(new ClientSystem(0, mMode, mQueue, args[0]))).start();
		
		// C:\Zone\javaworkspace\ForFinal\result\Search Image Dataset\YouTube-Tag\Comedy\2		
		(new Thread(new ServerSystem(0, mMode, mQueue, args[1]))).start();
				
		//mServerSystem.FindBestMatching();
		//mClientSystem.recvServerDatas(mServerSystem.getSuggestedTags());		
	}
}
