package com.skytbest.numberpickertimer.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import java.lang.reflect.*;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "Number Picker Timer";
    private MyCountDownTimer countDownTimer;
    private boolean timerHasStarted = false;
    private Button startButton;
    public TextView text;
    private final long INTERVAL = 1000;

    NumberPicker numberPicker1, numberPicker2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"Entering onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) this.findViewById(R.id.button);
        startButton.setOnClickListener(this);
        text = (TextView) this.findViewById(R.id.timer);

        //Create NumberPickers
        numberPicker1 = (NumberPicker) findViewById(R.id.numberPicker1);
        numberPicker2 = (NumberPicker) findViewById(R.id.numberPicker2);

        //Set min and max values for NumberPickers
        numberPicker1.setMaxValue(1000);
        numberPicker1.setMinValue(0);
        numberPicker2.setMaxValue(59);  //This is the seconds picker
        numberPicker2.setMinValue(0);
        numberPicker2.setFormatter(new MyTwoDigitFormatter());

        Log.i(TAG, "Exiting onCreate()");
    }

    @Override
    public void onClick(View v) {
        //Calculate total time from NumberPickers in seconds
        long startTime = (numberPicker1.getValue() * 60) * 1000 + numberPicker2.getValue() * 1000;
        Log.i(TAG, "Start time: " + startTime + "");


        if(!timerHasStarted) {

            //Create CountDownTimer with values from NumberPickers
            countDownTimer = new MyCountDownTimer(startTime, INTERVAL);
            text.setText(text.getText() + String.valueOf(startTime / 1000));    //should be removed

            countDownTimer.start();
            timerHasStarted = true;

            //Disable the NumberPickers after 'Start' is pressed
            numberPicker1.setEnabled(false);
            numberPicker2.setEnabled(false);

            startButton.setText(R.string.stop);
        } else {
            countDownTimer.cancel();
            //countDownTimer = countDownTimer.pause();
            timerHasStarted = false;

            //Re-enable the NumberPickers after 'Start' is pressed
            numberPicker1.setEnabled(true);
            numberPicker2.setEnabled(true);

            startButton.setText(R.string.restart);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * using reflection to change the value because
     * changeValueByOne is a private function and setValue
     * doesn't call the onValueChange listener.
     *
     * @param numberPicker
     *            the higher picker
     * @param increment
     *            the increment
     */
    private void changeValueByOne(final NumberPicker numberPicker, final boolean increment) {

        Method method;
        try {
            // reflection call for
            method = numberPicker.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(numberPicker, increment);

        } catch (final NoSuchMethodException e) {
            //Back-up code in case the changeValueByOne method does not exist
            numberPicker.setValue(numberPicker.getValue() - 1);
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public class MyCountDownTimer extends CountDownTimer {
        private long millisUntilFinished;

        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            //Decrement seconds one more time to get it = '00'
            if(numberPicker2.getValue() > 0){
                changeValueByOne(numberPicker2, false);
            }

            text.setText(R.string.timeUp);
            startButton.setText(R.string.start);

            //re-enable the NumberPickers once countdown is done
            numberPicker1.setEnabled(true);
            numberPicker2.setEnabled(true);

            //set timeHasStarted variable back to false because timer is stopped
            timerHasStarted = false;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            text.setText("" + millisUntilFinished / 1000);

            //Decrement the NumberPickers after each second
            if(numberPicker2.getValue() > 0) {
                //When seconds left is greater than 0 decrement seconds
                changeValueByOne(numberPicker2, false);
            }
            else if(numberPicker1.getValue() > 0 && numberPicker2.getValue() <= 0) {
                //Else, if seconds <= 0 and minutes > 0 decrement minutes and reset seconds to 59
                changeValueByOne(numberPicker1, false);
                numberPicker2.setValue(59);
            }
            else if(numberPicker1.getValue() <= 0 && numberPicker2.getValue() <= 0){
                //Else, if both minutes and seconds <= 0 revert back to 0 and finish
                //This should never really happen, but just in case
                numberPicker1.setValue(0);
                numberPicker2.setValue(0);
                Log.i(TAG, "There is a tick when both are 0");
            }

            this.millisUntilFinished = millisUntilFinished;
        }

    }

    //This formatter will be used to make numberPicker2 two digits wide even when < 10
    public class MyTwoDigitFormatter implements Formatter {
        public String format(int value) {
            return String.format("%02d", value);
        }
    }
}
