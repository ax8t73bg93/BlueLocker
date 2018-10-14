package jbhunt.hackathon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;

public class Login extends AppCompatActivity {


    private AssetsPropertyReader assetsPropertyReader;
    private Context context;
    private Properties p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        assetsPropertyReader = new AssetsPropertyReader(context);
        p = assetsPropertyReader.getProperties("BlueCrypt.properties");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText password = (EditText) findViewById(R.id.passwordField);
        password.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6)
                {
                    try {
                        validate(s);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void validate(Editable s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(s.toString().getBytes("iso-8859-1"), 0, s.length());
        final byte[] sha1hash = md.digest();
        s.clear();

        if(p.getProperty("pinHash").isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setMessage("Setting PIN.. Are you sure?")
                    .setTitle("New PIN")
                    .setPositiveButton("Of course", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            p.setProperty("pinHash", Arrays.toString(sha1hash));

                            Intent newUser = new Intent(context, loggedinActivity.class);
                            newUser.putExtra("isCurrent", false);
                            newUser.putExtra("properties", p);
                            startActivity(newUser);
                        }
                    })
                    .setNegativeButton("Let me Try Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if (Arrays.toString(sha1hash).equals(p.getProperty("pinHash")))
        {
            Intent currentUser = new Intent(context, loggedinActivity.class);
            currentUser.putExtra("isCurrent", true);
            currentUser.putExtra("properties", p);
            startActivity(currentUser);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setMessage("Wrong PIN")
                    .setTitle("Incorrect")
                    .setNeutralButton("Sigh, okay.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class AssetsPropertyReader {
        private Context context;
        private Properties properties;

        public AssetsPropertyReader(Context context) {
            this.context = context;
            /**
             * Constructs a new Properties object.
             */
            properties = new Properties();
        }

        public Properties getProperties(String FileName) {

            try {
                /**
                 * getAssets() Return an AssetManager instance for your
                 * application's package. AssetManager Provides access to an
                 * application's raw asset files;
                 */
                AssetManager assetManager = context.getAssets();
                /**
                 * Open an asset using ACCESS_STREAMING mode. This
                 */
                InputStream inputStream = assetManager.open(FileName);
                /**
                 * Loads properties from the specified InputStream,
                 */
                properties.load(inputStream);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.e("AssetsPropertyReader", e.toString());
            }
            return properties;
        }
    }
}
