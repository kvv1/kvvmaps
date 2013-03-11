package forth;

import java.io.IOException;
import java.io.InputStream;

public class Input {
	public Input(InputStream is) {
		this.is = is;
	}
	
	private InputStream is;

	public String getToken() {
		int c;
		try {
			while((c = is.read()) <= ' ') {
				if(c == -1)
					return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		while(c > ' ') {
			sb.append((char)c);
			try {
				c = is.read();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		return sb.toString();
	}

	public void skipLine() {
		int c;
		try {
			while((c = is.read()) >= ' ') {
				if(c == -1)
					return ;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
