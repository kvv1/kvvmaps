package kvv.mailru;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CounterReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		int n = intent.getIntExtra(CounterService.SET_COUNTER_INTENT_PARAM, 0);

		Intent newIntent = new Intent(context, CounterService.class);
		newIntent.putExtra(CounterService.SET_COUNTER_INTENT_PARAM, n);

		context.startService(newIntent);
	}

}
