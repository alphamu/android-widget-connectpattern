package com.bcgdv.asia.connectpattern;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bcgdv.asia.lib.connectpattern.ConnectPatternView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ConnectPatternView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = (ConnectPatternView) findViewById(R.id.connect);
        view.animateIn();

        view.setOnConnectPatternListener(new ConnectPatternView.OnConnectPatternListener() {
            @Override
            public void onPatternEntered(ArrayList<Integer> result) {
                //in this example we animate the widget out as soon as the user connects 3 points
                if (result.size() == 3) {
                    view.animateOut();
                }
            }

            @Override
            public void onPatternAbandoned() {

            }

            @Override
            public void animateInStart() {

            }

            @Override
            public void animateInEnd() {

            }

            @Override
            public void animateOutStart() {

            }

            @Override
            public void animateOutEnd() {
                view.setVisibility(View.GONE);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.animateIn();
                    }
                }, 1000);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}