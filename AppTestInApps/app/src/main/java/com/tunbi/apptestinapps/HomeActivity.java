package com.tunbi.apptestinapps;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by TungLT on 9/28/15.
 */
public class HomeActivity extends Activity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initControls();
    }

    private void initControls() {
        findViewById(R.id.btnBuy).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBuy:
                break;
            default:
                break;
        }
    }

    private void buildDialog(){

    }
}
