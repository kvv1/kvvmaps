package kvv.goniometer;

public interface Props {
	String get(String name, String defaultValue);
	int getInt(String name, int defaultValue);
	float getFloat(String name, float defaultValue);
}
