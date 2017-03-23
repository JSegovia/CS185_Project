package com.sendbird.android.CS185Project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

public class MainActivity extends FragmentActivity {
    public static String VERSION = "3.0.22.0";

    private enum State {DISCONNECTED, CONNECTING, CONNECTED}

    static boolean connected = false;
    /**
     * To test push notifications with your own appId, you should replace google-services.json with yours.
     * Also you need to set Server API Token and Sender ID in SendBird dashboard.
     * Please carefully read "Push notifications" section in SendBird Android documentation
     */
    //here
    private static final String appId = "51C862E6-86D3-4E6E-9858-6A9A40B1F43A";

    public static String sUserId;
    private String mNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sUserId = getPreferences(Context.MODE_PRIVATE).getString("user_id", "");
        mNickname = sUserId;

        SendBird.init(appId, this);

        ((EditText) findViewById(R.id.etxt_user_id)).setText(sUserId);
        ((EditText) findViewById(R.id.etxt_user_id)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sUserId = s.toString();
                mNickname = sUserId;
            }
        });

       // ((TextInputEditText) findViewById(R.id.etxt_nickname)).setText(mNickname);
        /*((TextInputEditText) findViewById(R.id.etxt_nickname)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNickname = s.toString();
            }
        });*/

        findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button) view;
                if (btn.getText().equals("Connect")) {
                    btn.setText("Connecting...");
                    connect();
                    SystemClock.sleep(2000);
                    Intent intent = new Intent(MainActivity.this, GroupChannelListActivity.class);
                    intent.putExtra("user", sUserId);

                    startActivity(intent);
                } else {
                    disconnect();
                }

                Helper.hideKeyboard(MainActivity.this);
            }
        });

        /*findViewById(R.id.btn_open_channel_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SendBirdOpenChannelListActivity.class);
                startActivity(intent);
            }
        });*/

        /*findViewById(R.id.btn_group_channel_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GroupChannelListActivity.class);
                startActivity(intent);
            }
        });*/

        TextView versionText = (TextView) findViewById(R.id.txt_sendbird_version);
        versionText.setText(String.format(getResources().getString(R.string.sendbird_version), SendBird.getSDKVersion()));

        setState(State.DISCONNECTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SendBird.disconnect(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * If the minimum SDK version you support is under Android 4.0,
         * you MUST uncomment the below code to receive push notifications.
         */
//        SendBird.notifyActivityResumedForOldAndroids();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * If the minimum SDK version you support is under Android 4.0,
         * you MUST uncomment the below code to receive push notifications.
         */
//        SendBird.notifyActivityPausedForOldAndroids();
    }

    private void setState(State state) {
        switch (state) {
            case DISCONNECTED:
                ((Button) findViewById(R.id.btn_connect)).setText("Connect");
                findViewById(R.id.btn_connect).setEnabled(true);
               // findViewById(R.id.btn_open_channel_list).setEnabled(false);
                //findViewById(R.id.btn_group_channel_list).setEnabled(false);
                break;

            case CONNECTING:
                ((Button) findViewById(R.id.btn_connect)).setText("Connecting...");
                findViewById(R.id.btn_connect).setEnabled(false);
               // findViewById(R.id.btn_open_channel_list).setEnabled(false);
                //findViewById(R.id.btn_group_channel_list).setEnabled(false);
                break;

            case CONNECTED:
                connected = true;
                ((Button) findViewById(R.id.btn_connect)).setText("Disconnect");
                findViewById(R.id.btn_connect).setEnabled(true);

            //    findViewById(R.id.btn_open_channel_list).setEnabled(true);
                //findViewById(R.id.btn_group_channel_list).setEnabled(true);
                break;
        }
    }

    private void connect() {
        SendBird.connect(sUserId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (e != null) {
                  //  Toast.makeText(MainActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setState(State.DISCONNECTED);
                    return;
                }

                String nickname = sUserId;

                SendBird.updateCurrentUserInfo(nickname, null, new SendBird.UserInfoUpdateHandler() {
                    @Override
                    public void onUpdated(SendBirdException e) {
                        if (e != null) {
                           // Toast.makeText(MainActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            setState(State.DISCONNECTED);

                        }

                        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                        editor.putString("user_id", sUserId);
                        editor.putString("nickname", sUserId);
                        editor.commit();

                        setState(State.CONNECTED);
                    }
                });

                if (FirebaseInstanceId.getInstance().getToken() == null) return;

                SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(), true, new SendBird.RegisterPushTokenWithStatusHandler() {
                    @Override
                    public void onRegistered(SendBird.PushTokenRegistrationStatus pushTokenRegistrationStatus, SendBirdException e) {
                        if (e != null) {
                         //   Toast.makeText(MainActivity.this, "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
            }
        });

        setState(State.CONNECTING);
    }

    private void disconnect() {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                setState(State.DISCONNECTED);
            }
        });
    }
}
