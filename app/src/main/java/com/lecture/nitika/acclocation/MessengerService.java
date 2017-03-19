package com.lecture.nitika.acclocation;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

public class MessengerService extends Service {
    //we cam define different msg types that can be passed form our lactivity
    public static final int MSG_ACTIVITY=1;
    public MessengerService() {
    }
    final Messenger mymessenger=new Messenger(new myHandler());
    class myHandler extends Handler{
        @Override
        public void handleMessage(Message msg) { //havinf msg from the activity
            switch (msg.what){//getting the msg type
                case MSG_ACTIVITY:
                    Toast.makeText(getApplicationContext(),"Hello from activity",Toast.LENGTH_LONG).show();
                    break;
                default:
                    super.handleMessage(msg);

            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {//returns null for started service
        // TODO: Return the communication channel to the service.
        return mymessenger.getBinder();
    }
}
