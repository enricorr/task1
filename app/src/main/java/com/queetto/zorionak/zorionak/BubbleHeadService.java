package com.queetto.zorionak.zorionak;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Calendar;

public class BubbleHeadService extends Service {


    private WindowManager mWindowManager;
    private View mChatHeadView;
    private Uri uriImage;
    private String uriString;
    private String nombre;
    private int dia;
    private int mes;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras!=null) {
            nombre = extras.getString("nombre");
            dia = extras.getInt("dia");
            mes = extras.getInt("mes");
            uriString = extras.getString("uri");
        }

        if (uriString!=null && !uriString.equals("")) {
            uriImage = Uri.parse(uriString);
        }
        creation();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void creation() {
        super.onCreate();


        //Inflate the chat head layout we created
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.layout_chat_head, null);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //Add the view to the window.
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //Specify the chat head position
        params.gravity = Gravity.TOP | Gravity.START;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);

        //Set the close button.
        ImageView closeButton = mChatHeadView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(v -> stopSelf());

        //Drag and move chat head using user's touch action.
        ImageView chatHeadImage = mChatHeadView.findViewById(R.id.bubble_head_profile_iv);
        if (uriImage!=null) {
            chatHeadImage.setImageURI(uriImage);
            chatHeadImage.setRotation(90);
        } else {
            chatHeadImage.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            private static final int MAX_CLICK_DURATION = 200;
            private long startClickTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identify if the user clicked the view or not.
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if(clickDuration < MAX_CLICK_DURATION) {
                            //Open the chat conversation click.
                            mChatHeadView.performClick();
                            Intent intent = new Intent(BubbleHeadService.this, BubbleActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("nombre", nombre);
                            extras.putInt("dia", dia);
                            extras.putInt("mes", mes);
                            extras.putString("uri", uriString);
                            intent.putExtras(extras);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            //close the service and remove the chat heads
                            stopSelf();
                        }
/*                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click.
                            mChatHeadView.performClick();
                            Intent intent = new Intent(BubbleHeadService.this, BubbleActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString("nombre", nombre);
                            extras.putInt("dia", dia);
                            extras.putInt("mes", mes);
                            extras.putString("uri", uriString);
                            intent.putExtras(extras);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            //close the service and remove the chat heads
                            stopSelf();
                        }*/
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatHeadView != null) mWindowManager.removeView(mChatHeadView);
    }
}
