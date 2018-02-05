package top.yxgu.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author blc
 *
 */
public class OSInfo {
	
	private static Properties props = System.getProperties(); 			//获得系统属性集  
	private static Runtime rt = Runtime.getRuntime();					//运行时
	
	/**
	 * 操作系统构架
	 * @return
	 */
	public static String getOSArch() {
		return props.getProperty("os.arch"); 		//操作系统构架
	}
	
	/**
	 * 操作系统名称
	 * @return
	 */
	public static String getOSName() {
		return props.getProperty("os.name"); 		//操作系统名称 
	}
	
	/**
	 * 操作系统版本
	 * @return
	 */
	public static String getOSVersion() {
		return props.getProperty("os.version"); 	//操作系统版本
	}
	
	/**
	 * cpu数
	 * @return
	 */
	public static int getProcessorNum() {
		return rt.availableProcessors();		//cpu数
	}
	
	public static int getJVMTotalMemorySize(){
		return (int)Runtime.getRuntime().totalMemory()/1024 /1024;
	}
	
	public static int getJVMFreeMemorySize(){
		return (int)Runtime.getRuntime().freeMemory() /1024 /1024;
	}
	
	public static InetAddress getHostAddress() {
	    try {
	        InetAddress candidateAddress = null;
	        // 遍历所有的网络接口
	        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
	            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            // 在所有的接口下再遍历IP
	            for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
	                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
	                    if (inetAddr.isSiteLocalAddress()) {
	                        // 如果是site-local地址，就是它了
	                    	System.out.println(inetAddr.toString());
	                        return inetAddr;
	                    } else if (candidateAddress == null) {
	                        // site-local类型的地址未被发现，先记录候选地址
	                        candidateAddress = inetAddr;
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            return candidateAddress;
	        }
	        // 如果没有发现 non-loopback地址.只能用最次选的方案
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        return jdkSuppliedAddress;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
}
