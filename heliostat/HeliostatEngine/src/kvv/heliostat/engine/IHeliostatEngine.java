package kvv.heliostat.engine;


public interface IHeliostatEngine{
	int TABLE_DAY_STEP = 10;
	int TABLE_LAST_DAY = 182;
	int TABLE_HOUR_STEP = 1;
	int TABLE_FIRST_HOUR = 5;
	int TABLE_LAST_HOUR = 19;
	
	void setTables(int[][] azTable, int[][] altTable);

	void start(int stepMS);
	void stop();

	void setPeriferial(ISensor );
}
