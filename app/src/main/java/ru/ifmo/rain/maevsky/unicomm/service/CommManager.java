package ru.ifmo.rain.maevsky.unicomm.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;

public class CommManager extends Service {
    private Keeper keeper = null;

    public CommManager() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new AsyncTask<Void, Void, Keeper>() {
            Keeper instance;

            @Override
            protected void onPostExecute(Keeper keeper) {
                CommManager.this.keeper = instance;
            }

            @Override
            protected Keeper doInBackground(Void... params) {
                return Keeper.getInstance(CommManager.this);
            }
        }.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    public void reload() {
        Keeper.instance = null;
        keeper = null;
        keeper = Keeper.getInstance(this);
    }

    @Override
    public Channel onBind(Intent intent) {
        return new Channel();
    }

    public class Channel extends Binder {
        public Keeper getKeeper() {
            return keeper == null ? Keeper.getInstance(CommManager.this) : keeper;
        }

        public void reloadKeeper() {
            reload();
        }
    }
}
