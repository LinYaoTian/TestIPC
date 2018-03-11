package com.rdc.testipc;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.app.NavUtils;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookManagerService extends Service {

    public static final String TAG = "BMS";

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);
        }
    };

    private void onNewBookArrived(Book book) throws RemoteException{
        mBookList.add(book);
        Log.d(TAG, "onNewBookArrived , notify listener");
        int N = mListenerList.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener iOnNewBookArrivedListener =
                    mListenerList.getBroadcastItem(i);
            if (iOnNewBookArrivedListener != null){
                iOnNewBookArrivedListener.onNewBookArrived(book);
            }
        }
        mListenerList.finishBroadcast();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book("111"));
        mBookList.add(new Book("222"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!mIsServiceDestroyed.get()){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Book book = new Book("555");
                    try {
                        onNewBookArrived(book);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        int check = checkCallingOrSelfPermission("com.rdc.testipc.permission.ACCESS_BOOK");
        if (check == PackageManager.PERMISSION_DENIED){
            return null;
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        mIsServiceDestroyed.set(true);
        super.onDestroy();

    }
}
