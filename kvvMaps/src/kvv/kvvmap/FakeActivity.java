package kvv.kvvmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FakeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Intent intent = new Intent(this, MyActivity.class);

//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
//		startActivity(intent);

		finish();
	}

}
