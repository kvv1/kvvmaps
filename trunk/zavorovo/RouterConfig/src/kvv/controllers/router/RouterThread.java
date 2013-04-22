package kvv.controllers.router;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

public class RouterThread extends Thread {
	private volatile boolean stopped;

	private TelnetClient tc = null;

	private final long routerCheckTime;
	private final String routerPassword;
	private final String routerPublicIP;
	private final String routerLocalIP;
	private final String routerGatewayIP;

	public RouterThread(long routerCheckTime, String routerPassword,
			String routerPublicIP, String routerLocalIP, String routerGatewayIP) {
		this.routerCheckTime = routerCheckTime;
		this.routerPassword = routerPassword;
		this.routerPublicIP = routerPublicIP;
		this.routerLocalIP = routerLocalIP;
		this.routerGatewayIP = routerGatewayIP;

		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	@SuppressWarnings("deprecation")
	public void stopThread() {
		stopped = true;
		stop();
	}
	
	@Override
	public void run() {
		while (!stopped) {
			try {
				tc = new TelnetClient();
				try {
					tc.addOptionHandler(new TerminalTypeOptionHandler("VT100",
							false, false, true, false));
				} catch (InvalidTelnetOptionException e) {
					System.err.println("Error registering option handlers: "
							+ e.getMessage());
				}
				tc.connect(routerGatewayIP, 23);
				OutputStream outstr = tc.getOutputStream();
				InputStream instr = tc.getInputStream();

				getResponse("admin", outstr, instr);
				getResponse(routerPassword, outstr, instr);

				// getResponse("iptables -t nat -F", outstr, instr);
				String iptables = getResponse("iptables -t nat -L", outstr,
						instr);

				// System.out.println("iptables =  "
				// + iptables);

				if (iptables.contains("POSTROUTING")
						&& !iptables.contains("to:" + routerLocalIP + ":80")) {

					System.out.println("ROUTER adding rule 80.");

					getResponse("iptables -t nat -I PREROUTING -p tcp -d "
							+ routerPublicIP
							+ " --dport 80 -j DNAT --to-destination "
							+ routerLocalIP + ":80", outstr, instr);
				}

				if (iptables.contains("POSTROUTING")
						&& !iptables.contains("to:" + routerLocalIP + ":3389")) {

					System.out.println("ROUTER adding rule 3389.");

					getResponse("iptables -t nat -I PREROUTING -p tcp -d "
							+ routerPublicIP
							+ " --dport 33389 -j DNAT --to-destination "
							+ routerLocalIP + ":3389", outstr, instr);
				}

				tc.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(routerCheckTime * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private String getResponse(String cmd, OutputStream os, InputStream is)
			throws Exception {
		os.write(cmd.getBytes());
		os.write('\n');
		os.flush();

		long time = System.currentTimeMillis();

		StringBuilder sb = new StringBuilder();

		while (System.currentTimeMillis() - time < 1000) {
			Thread.sleep(100);
			if (is.available() > 0) {
				byte[] buff = new byte[1024];
				int ret_read = 0;
				ret_read = is.read(buff);
				String resp = new String(buff, 0, ret_read);
				sb.append(resp);
			}
		}

		System.err.print("\n" + cmd + " --> " + sb);
		return sb.toString();
	}
}
