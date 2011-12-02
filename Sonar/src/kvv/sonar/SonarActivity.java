package kvv.sonar;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class SonarActivity extends Activity {
	
	private SonarView view;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = (SonarView) findViewById(R.id.view1);
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

    	if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            
            int[] data = new int[100];
            
            data[10] = 100;
            data[20] = -50;
            data[90] = 75;
            
            view.setData(data);
    		
    		return true;
    	}
    	return false;
    }
}