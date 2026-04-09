package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Setup Shoulder Buttons (LT, LB, RT, RB)
        setupButton(findViewById(R.id.btn_lt), "LT");
        setupButton(findViewById(R.id.btn_lb), "LB");
        setupButton(findViewById(R.id.btn_rt), "RT");
        setupButton(findViewById(R.id.btn_rb), "RB");

        // 2. Setup D-Pad Arrows (F = Forward, B = Backward, L = Left, R = Right)
        setupButton(findViewById(R.id.btn_up), "F");
        setupButton(findViewById(R.id.btn_down), "B");
        setupButton(findViewById(R.id.btn_left), "L");
        setupButton(findViewById(R.id.btn_right), "R");

        // 3. Setup Action Buttons
        setupButton(findViewById(R.id.btn_y), "Y");
        setupButton(findViewById(R.id.btn_a), "A");
        setupButton(findViewById(R.id.btn_x), "X");
        setupButton(findViewById(R.id.btn_b), "B");

        // 4. Setup Joysticks (J1 = Left, J2 = Right)
        setupJoystick(findViewById(R.id.left_joy_base), findViewById(R.id.left_joy_stick), "J1");
        setupJoystick(findViewById(R.id.right_joy_base), findViewById(R.id.right_joy_stick), "J2");
    }

    // --- BUTTON TOUCH LOGIC ---
    @SuppressLint("ClickableViewAccessibility")
    private void setupButton(View view, String command) {
        if (view != null) {
            view.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Jab finger button par lagegi, toh command bhejo
                    sendBluetoothCommand(command);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Jab finger hata lo, toh STOP command bhejo
                    sendBluetoothCommand("S");
                }
                return false;
            });
        }
    }

    // --- JOYSTICK TOUCH LOGIC ---
    @SuppressLint("ClickableViewAccessibility")
    private void setupJoystick(FrameLayout base, View stick, String joyName) {
        if (base == null || stick == null) return;

        base.setOnTouchListener(new View.OnTouchListener() {
            float centerX, centerY, baseRadius, stickRadius;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (baseRadius == 0) {
                    baseRadius = base.getWidth() / 2f;
                    stickRadius = stick.getWidth() / 2f;
                    centerX = baseRadius;
                    centerY = baseRadius;
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    float x = event.getX();
                    float y = event.getY();

                    float displacementX = x - centerX;
                    float displacementY = y - centerY;
                    float distance = (float) Math.sqrt(Math.pow(displacementX, 2) + Math.pow(displacementY, 2));
                    float maxDistance = baseRadius - stickRadius;

                    if (distance > maxDistance) {
                        float ratio = maxDistance / distance;
                        displacementX *= ratio;
                        displacementY *= ratio;
                    }

                    stick.setTranslationX(displacementX);
                    stick.setTranslationY(displacementY);

                    // Math: Convert physical drag into -100 to 100 percentage
                    int mappedX = (int) ((displacementX / maxDistance) * 100);
                    int mappedY = (int) ((-displacementY / maxDistance) * 100); // Inverted so 'Up' is positive

                    // Format: "J1:50,-20"
                    sendBluetoothCommand(joyName + ":" + mappedX + "," + mappedY);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Chhodne par stick wapas center mein
                    stick.setTranslationX(0);
                    stick.setTranslationY(0);
                    sendBluetoothCommand(joyName + ":0,0"); // Send reset command
                }
                return true;
            }
        });
    }

    // --- BLUETOOTH COMMAND SENDER ---
    private void sendBluetoothCommand(String command) {
        // Yeh function check karne ke liye screen par message dikhayega.
        // Asli HC-05 Bluetooth ka code hum is function ke andar next step mein likhenge!
        Toast.makeText(this, "Sending: " + command, Toast.LENGTH_SHORT).show();
    }
}