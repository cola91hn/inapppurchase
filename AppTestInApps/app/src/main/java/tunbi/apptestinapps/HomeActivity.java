package tunbi.apptestinapps;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.android.vending.billing.IInAppBillingService;
import com.tunbi.apptestinapps.R;

/**
 * Created by TungLT on 9/28/15.
 */
public class HomeActivity extends Activity implements View.OnClickListener {
    private Dialog dialog;
    IInAppBillingService mService;
    ServiceConnection serviceConnection;
    String inAppId = "android.test.purchased";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initControls();
    }

    private void initControls() {
        findViewById(R.id.btnBuy).setOnClickListener(this);
        buildDialog(HomeActivity.this);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };

        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBuy:
                dialog.show();
                break;

            default:
                break;
        }
    }

    private void buildDialog(Context mContext) {
        dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.layout_dialog);
        dialog.setTitle("Buy method");
        CheckBox rd1 = (CheckBox) dialog.findViewById(R.id.rbOneMonth);
        CheckBox rd2 = (CheckBox) dialog.findViewById(R.id.rbThreeMonth);
        CheckBox rd3 = (CheckBox) dialog.findViewById(R.id.rbSixthMonth);
        CheckBox rd4 = (CheckBox) dialog.findViewById(R.id.rbTelMonth);
        Button btnAgree = (Button) dialog.findViewById(R.id.btnAgree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
