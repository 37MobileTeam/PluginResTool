package com.sq.tool;


public class MachineUtils {

	/**是否是windows操作系统，否则是UNIX类的操作系统
	 * @return
	 */
	public static boolean isWindowsOS(){
		
		String osName  = System.getProperty("os.name");

		if (osName== null){
			return false;
		}else {
			 if (osName.contains("Windows")) {
					return true;
				}else {
					return false;
				}
		}
	}
	
	public  static final int IS_UNKONOW_OS = 0;
	public  static final int IS_WINDOWS_OS = 1;
	public  static final int IS_MAC_OS = 2;
	public  static final int IS_LINUX_OS = 3;
	
	public static int getOSName(){
		
		String osName  = System.getProperty("os.name");

		if (osName== null){
			return IS_UNKONOW_OS;
		}else {
			 if (osName.contains("Windows") || osName.contains("windows")) {
					return IS_WINDOWS_OS;
				}else if (osName.contains("Mac") || osName.contains("mac")) {
					return IS_MAC_OS;
				}else if (osName.contains("Linux") || osName.contains("linux")) {
					return IS_LINUX_OS;
				}else {
					return IS_UNKONOW_OS;
				}
		}
	}
	
}

