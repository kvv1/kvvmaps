package kvv.httpserver;

public abstract class HTTPHandler {
	public final String path;
	public abstract String handle(String queryString);
	
	public HTTPHandler(String path) {
		this.path = path;
	}
}
