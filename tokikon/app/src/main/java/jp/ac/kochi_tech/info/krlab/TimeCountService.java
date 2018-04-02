package jp.ac.kochi_tech.info.krlab;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TimeCountService extends Service {

    public TimeCountService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
