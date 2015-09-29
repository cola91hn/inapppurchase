package tunbi.apptestinapps;

import android.app.Activity;
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
import android.widget.Toast;

import com.tunbi.apptestinapps.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import tunbi.apptestinapps.util.IabHelper;
import tunbi.apptestinapps.util.IabResult;


public class MainActivity extends Activity implements OnClickListener {

    private Toast mToast;
    private String base64EncodedPublicKey = "";
    private IabHelper mHelper;
    String TAG = "MainActivity";
    int REQUESTCODE_PICK_FILE = 1001;
    Button btnSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
    }

    private void initControls() {
        mToast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG);
        findViewById(R.id.btnBrowser).setOnClickListener(this);
        btnSetup = (Button) findViewById(R.id.btnSetup);
        btnSetup.setEnabled(false);
        btnSetup.setOnClickListener(this);
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
            readFile(f);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBrowser:
                pickFileFromStorage();
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
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            Log.d(TAG, "In-app Billing is set up OK " + result);
                        }
                    }
                });

                break;
            default:
                break;
        }
    }
}
