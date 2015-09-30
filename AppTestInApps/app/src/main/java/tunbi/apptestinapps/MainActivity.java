package tunbi.apptestinapps;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.tunbi.apptestinapps.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import tunbi.apptestinapps.util.IabHelper;
import tunbi.apptestinapps.util.IabResult;
import tunbi.apptestinapps.util.Inventory;
import tunbi.apptestinapps.util.Purchase;


public class MainActivity extends Activity implements OnClickListener {

    private Toast mToast;
    private String base64EncodedPublicKey = "";
    private IabHelper mHelper;
    String TAG = "MainActivity";
    int REQUESTCODE_PICK_FILE = 1001;
    Button btnSetup, btnBrowser,btnBuy;
    private EditText edtOnce, edtThree, edtSixth, edtTwel;
    private Dialog dialog;
    private  String ITEM_SKU="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
    }

    private void initControls() {
        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG);
        btnBrowser = (Button) findViewById(R.id.btnBrowser);
        btnSetup = (Button) findViewById(R.id.btnSetup);
        btnBuy = (Button) findViewById(R.id.btnBuy);
        edtOnce = (EditText) findViewById(R.id.edtOption1);
        edtThree = (EditText) findViewById(R.id.edtOption2);
        edtSixth = (EditText) findViewById(R.id.edtOption3);
        edtTwel = (EditText) findViewById(R.id.edtOption4);
        btnSetup.setEnabled(false);
        btnBrowser.setOnClickListener(this);
        btnSetup.setOnClickListener(this);
        btnBuy.setOnClickListener(this);
        buildDialog(MainActivity.this);
    }

    private void pickFileFromStorage() {
        Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mediaIntent.setType("application/txt");
        startActivityForResult(mediaIntent, REQUESTCODE_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_PICK_FILE
                && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();
            File f = new File(fileUri.getPath());
            btnBrowser.setText(fileUri.getPath());
            readFile(f);
        }else if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void readFile(File file) {
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin);

            byte[] contents = new byte[1024];
            int bytesRead = 0;
            String strFileContents = "";
            while ((bytesRead = bin.read(contents)) != -1) {
                strFileContents = new String(contents, 0, bytesRead);
            }
            base64EncodedPublicKey = strFileContents;
            if (!base64EncodedPublicKey.isEmpty()) {
                btnSetup.setEnabled(true);
                mHelper = new IabHelper(this, base64EncodedPublicKey);
            } else {
                mToast.setText("Public key not empty.");
                mToast.show();
            }
            bin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildDialog(Context mContext) {
        dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.layout_dialog);
        dialog.setTitle("Buy method");
        final RadioButton rd1 = (RadioButton) dialog.findViewById(R.id.rbOneMonth);
        final RadioButton rd2 = (RadioButton) dialog.findViewById(R.id.rbThreeMonth);
        final RadioButton rd3 = (RadioButton) dialog.findViewById(R.id.rbSixthMonth);
        final RadioButton rd4 = (RadioButton) dialog.findViewById(R.id.rbTelMonth);
        Button btnAgree = (Button) dialog.findViewById(R.id.btnAgree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rd1.isChecked()){
                    ITEM_SKU = edtOnce.getText().toString();
                }else if(rd2.isChecked()){
                    ITEM_SKU = edtThree.getText().toString();
                } else if(rd3.isChecked()){
                    ITEM_SKU = edtSixth.getText().toString();
                }else if(rd4.isChecked()){
                    ITEM_SKU = edtTwel.getText().toString();
                }
                mHelper.launchPurchaseFlow(MainActivity.this, ITEM_SKU, 10001,
                        mPurchaseFinishedListener, "mypurchasetoken");
                dialog.dismiss();
            }
        });
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            if (result.isFailure()) {
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                btnBuy.setEnabled(false);
            }

        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        btnSetup.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBrowser:
                pickFileFromStorage();
                break;
            case R.id.btnBuy:
                dialog.show();
                break;
            case R.id.btnSetup:
                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {
                        if (!result.isSuccess()) {
                            Log.d(TAG, "In-app Billing setup failed: " +
                                    result);
                        } else {
                            mToast.setText("Setup successful.");
                            mToast.show();
                            Log.d(TAG, "In-app Billing is set up OK " + result);
                        }
                    }
                });

                break;
            default:
                break;
        }


    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
