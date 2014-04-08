package kvv.controllers.server;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import kvv.controllers.client.ControllersService;
import kvv.controllers.register.AllRegs;
import kvv.controllers.server.context.Context;
import kvv.controllers.shared.ControllerDescr;
import kvv.controllers.utils.Constants;
import kvv.evlang.EG1;
import kvv.evlang.ParseException;
import kvv.evlang.Token;
import kvv.evlang.impl.ExtRegisterDescr;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ControllersServiceImpl extends RemoteServiceServlet implements
		ControllersService {

	@Override
	public int getReg(int addr, int reg) throws Exception {
		return Context.getInstance().controller.getReg(addr, reg);
	}

	@Override
	public void setReg(int addr, int reg, int val) throws Exception {
		try {
			Context.getInstance().controller.setReg(addr, reg, val);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	public static  void print(AllRegs allRegs) {
		List<Integer> keys = new ArrayList<Integer>(allRegs.values.keySet());
		Collections.sort(keys);
		for(Integer reg : keys) {
			System.out.print(reg + ":" + allRegs.values.get(reg) + " ");
		}
		System.out.println();
	}
	
	@Override
	public AllRegs getRegs(int addr) throws Exception {
		try {
			AllRegs allRegs = Context.getInstance().controller.getAllRegs(addr);
			//print(allRegs);
			return allRegs;
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public String upload(int addr, String fileName) {
		EG1 parser = null;
		try {
			if (fileName == null) {
				Context.getInstance().controller.upload(addr, new byte[0]);
				storeSourceDescr(addr, null);
			} else {
				parser = new EG1(Constants.srcDir + fileName) {
					@Override
					protected ExtRegisterDescr getExtRegisterDescr(
							String extRegName) {
						return null;
					}
				};

				parser.parse(Context.getInstance().controllers.get(addr).type);
				byte[] bytes = parser.getRTContext().dump();
				Context.getInstance().controller.upload(addr, bytes);

				storeSourceDescr(addr, fileName);
			}
			return null;
		} catch (ParseException e) {
			Token t = parser.token;
			return " line " + t.beginLine + " : " + e.getMessage();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public void vmInit(int addr) throws Exception {
		try {
			Context.getInstance().controller.vmInit(addr);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

	private void storeSourceDescr(int addr, String gson) throws Exception {
		Properties props = new Properties();
		try {
			FileReader fr = new FileReader(Constants.srcFile);
			props.load(fr);
			fr.close();
		} catch (IOException e) {
		}
		try {
			ControllerDescr controllerDescr = Context.getInstance().controllers
					.get(addr);

			if (gson != null)
				props.setProperty(controllerDescr.name, gson);
			else
				props.remove(controllerDescr.name);

			FileWriter fw = new FileWriter(Constants.srcFile);
			props.store(fw, "");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public Integer hello(int addr) throws Exception {
		try {
			return Context.getInstance().controller.hello(addr);
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}
	}

}
