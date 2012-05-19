package kvv.controllers.server;

import java.io.InputStream;
import java.io.OutputStream;

import kvv.controllers.shared.Constants;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

public class RouterThread extends Thread {
	public static volatile RouterThread instance;
	public volatile boolean stopped;

	private TelnetClient tc = null;

	{
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
		start();
	}

	@Override
	public void run() {
		while (!stopped) {
			try {
				String routerPassword = Utils.getProp(Constants.propsFile,
						"routerPassword");
				String routerPublicIP = Utils.getProp(Constants.propsFile,
						"routerPublicIP");
				String routerLocalIP = Utils.getProp(Constants.propsFile,
						"routerLocalIP");
				long routerCheckTime = 1000L * Integer.valueOf(Utils.getProp(
						Constants.propsFile, "routerCheckTimeS"));
				tc = new TelnetClient();
				try {
					tc.addOptionHandler(new TerminalTypeOptionHandler("VT100",
							false, false, true, false));
				} catch (InvalidTelnetOptionException e) {
					System.err.println("Error registering option handlers: "
							+ e.getMessage());
				}
				tc.connect("192.168.1.1", 23);
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
					// getResponse("iptables -t nat -F", outstr, instr);

					System.out.println("ROUTER adding rule.");

					getResponse("iptables -t nat -I PREROUTING -p tcp -d "
							+ routerPublicIP
							+ " --dport 80 -j DNAT --to-destination "
							+ routerLocalIP + ":80", outstr, instr);
				}

				tc.disconnect();
				Thread.sleep(routerCheckTime);
			} catch (Exception e) {
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
