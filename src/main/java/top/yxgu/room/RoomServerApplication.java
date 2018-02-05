package top.yxgu.room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import top.yxgu.room.roomScoket.RoomSocketClient;
import top.yxgu.room.webSocket.ChannelManager;
import top.yxgu.room.webSocket.WebSocketServer;
import top.yxgu.utils.OSInfo;

@SpringBootApplication
public class RoomServerApplication {
	private static final Logger logger = LoggerFactory.getLogger(RoomServerApplication.class);

	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(RoomServerApplication.class);
		ApplicationContext ctx = app.run(args);
		
		runService(ctx);
		cmd(ctx);
	}
	
	private static void runService(ApplicationContext ctx) {
		RoomSocketClient roomServer = (RoomSocketClient)ctx.getBean(RoomSocketClient.class);
		WebSocketServer webSocketServer = (WebSocketServer)ctx.getBean(WebSocketServer.class);
		if (roomServer == null || webSocketServer == null) {
			logger.error("服务器启动出错");
			return ;
		}
		
		try {
			System.out.println( InetAddress.getLocalHost().getHostAddress() );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Thread() {
			@Override
	        public void run() {
				roomServer.run();
			}
		}.start();
		
		new Thread() {
			@Override
	        public void run() {
				webSocketServer.run();
			}
		}.start();
	}
	
	private static void cmd(ApplicationContext ctx) {
		RoomSocketClient roomServer = (RoomSocketClient)ctx.getBean(RoomSocketClient.class);
		WebSocketServer webSocketServer = (WebSocketServer)ctx.getBean(WebSocketServer.class);
		
		///// 接收命令
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
//		System.out.print("yxgu>");
		String inputStr;
		String scmd = null;
		String[] cmds;
		try {
			//初始调用一次，第一次调用会返回无效值
//			OSInfo.getCpuRatio();
			
			while ( (inputStr = br.readLine()) != null) {
				inputStr = inputStr.trim().toLowerCase();
				cmds = inputStr.split(" ");
				scmd = cmds[0];
				if ("q".equals(scmd) || "quit".equals(scmd)) {
					String reason = cmds.length == 1 ? "" : cmds[1];
					webSocketServer.stop(reason);
					roomServer.stop(reason);
					System.exit(SpringApplication.exit(ctx));
					break;
				} else if ("ka".equals(scmd) || "killall".equals(scmd)) {
//					as.killAllProcess();
				} else if ("inf".equals(scmd)) {
					System.out.println("yxgu>	System Information:");
					System.out.println("                               OS:	"+ OSInfo.getOSArch() + "  " + OSInfo.getOSName() + " V" + OSInfo.getOSVersion());
					System.out.println("                     CPU Core Num:	"+ OSInfo.getProcessorNum());
					System.out.println("                 Total JVM Memory:	"+ OSInfo.getJVMTotalMemorySize() + "M");
					System.out.println("                  Free JVM Memory:	"+ OSInfo.getJVMFreeMemorySize() + "M");
					System.out.println("                  Online User Num:	"+ ChannelManager.size());
//					System.out.println("        Use Ports(Client id:port):	"+ Arrays.toString(as.getUsePorts()));
					System.out.print("yxgu>");
				} else if ("?".equals(scmd) || "help".equals(scmd)) {
					System.out.println("yxgu>	command include:\n" +
							"             q/quit:	Exit server.\n" +
							"         ka/killall:	Kill all native process.\n" +
							"                inf:	Show system information.");
					System.out.print("yxgu>");
					
				} else {
					System.out.println("yxgu>Invalid command. Input \"help\" view command information.");
					System.out.print("yxgu>");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
