package com.rdc.testipc;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int MESSAGE = 1;
    private IBookManager mBookManager;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE:
                    Log.d(TAG, "receive new book :"+msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub(){
        //这里是运行在Binder线程池中的，不可以做UI更新操作
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MESSAGE,newBook).sendToTarget();
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        //都运行在主线程
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBookManager = IBookManager.Stub.asInterface(iBinder);
            try{
                Log.d(TAG, "onServiceConnected");
                mBookManager.registerListener(mOnNewBookArrivedListener);
                mBookManager.getBookList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBookManager = null;
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            //在客户端的Client的线程池中调用，不能更新UI
            if (mBookManager == null){
                return;
            }
            mBookManager.asBinder().unlinkToDeath(mDeathRecipient,0);
            mBookManager = null;
            Intent intent = new Intent(MainActivity.this,BookManagerService.class);
            bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this,BookManagerService.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (mBookManager != null
                && mBookManager.asBinder().isBinderAlive()){
            try {
                mBookManager.unregisterListener(mOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
