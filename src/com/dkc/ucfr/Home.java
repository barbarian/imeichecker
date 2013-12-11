package com.dkc.ucfr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity {
    private TextView imeiText;
    private TextView resText;
    private ProgressDialog progressDialog;
    private String imei_code="";
    private String result_message="";
    private static final String details_website = "http://www.ucrf.gov.ua/uk/imei_base";
    private static final String author_website = "http://dkc7dev.com/imei-checker";
    private static final String OTHER_APPS_URL = "https://play.google.com/store/apps/developer?id=dkc7dev";
    private static final int ABOUT_DIALOG = 1;
    private static final int HELP_DIALOG = 2;
    private static final int PROGRESS_DIALOG = 3;
    private static final int CHECK_IMEI_DIALOG =4;

    private static final String TEXT_IMEI_CODE = "TEXT_IMEI_CODE";
    private static final String TEXT_RES_MSG = "TEXT_RES_MSG";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initFields();

        restoreState(savedInstanceState);

        fillValues();
    }
    private void initFields()
    {
        imeiText = (TextView)findViewById(R.id.ImeiText);
        resText = (TextView)findViewById(R.id.TextResults);
        findViewById(R.id.checkImeiButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkImei(imei_code);
            }
        });
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    // This method is called once the menu is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // We have only one menu option
            case R.id.ShowSite:
                openDetailsWebSite();
                break;
            case R.id.SetImei:
                showDialog(CHECK_IMEI_DIALOG);
                break;
            case R.id.Help:
                showDialog(HELP_DIALOG);
                break;
            case R.id.About:
                showDialog(ABOUT_DIALOG);
                break;
            case R.id.MoreApps:
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(OTHER_APPS_URL));
                startActivity(myIntent);
                break;
        }
        return true;
    }
    private void restoreState(Bundle savedInstanceState){
        imei_code = (savedInstanceState == null) ? null :
                (String) savedInstanceState.getSerializable(TEXT_IMEI_CODE);
        result_message = (savedInstanceState == null) ? null :
                (String) savedInstanceState.getSerializable(TEXT_RES_MSG);

        if(imei_code==null||imei_code.length()==0){
            getImei();
        }
    }
    private void fillValues(){
        if(imei_code!=null){
            imeiText.setText(String.format(getString(R.string.msg_IMEI_code), imei_code));
        }

        if(result_message!=null){
            resText.setText(result_message);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CHECK_IMEI_DIALOG:
                AlertDialog imeiDialog=   createCheckIMEIDialog();
                return imeiDialog;
            case ABOUT_DIALOG:
                AlertDialog aboutDialog=   createOptionsAboutDialog();
                return aboutDialog;
            case HELP_DIALOG:
                AlertDialog helpDialog=  createOptionsHelpDialog();
                return helpDialog;
            case PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(this);

                progressDialog.setTitle(getString(R.string.wait));
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                return progressDialog;
            default:
                return null;
        }
    }

    private AlertDialog createCheckIMEIDialog()
    {
        final EditText input = new EditText(this);
        AlertDialog helpDialog= new AlertDialog.Builder(this)
                .setTitle(R.string.menu_set_imei).setMessage(R.string.set_imei_message)
                .setView(input)
                .setNegativeButton(R.string.help_cancel,null)
                .setPositiveButton(R.string.help_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();
                                checkImei(value);
                            }
                        })
                .create();
        return helpDialog;
    }

    private AlertDialog createOptionsHelpDialog()
    {
        AlertDialog helpDialog= new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.menu_help).setMessage(R.string.help_text)
                .setNeutralButton(R.string.help_more,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openDetailsWebSite();
                            }
                        })
                .setPositiveButton(R.string.help_ok,null)
                .create();
        return helpDialog;
    }

    private AlertDialog createOptionsAboutDialog()
    {
        AlertDialog aboutDialog= new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.menu_about).setMessage(R.string.about_text)
                .setNeutralButton(R.string.help_more,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openAuthorWebSite();
                            }
                        })
                .setPositiveButton(R.string.help_ok, null)
                .create();
        return aboutDialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            default:
                break;
        }
    }
    private void openDetailsWebSite(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(details_website));
        startActivity(i);
    }
    private void openAuthorWebSite(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(author_website));
        startActivity(i);
    }
    private void getImei(){
        TelephonyManager tManager = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        imei_code=tManager.getDeviceId();
    }
    private void checkImei(String code) {
        result_message =null;
        fillValues();
        new checkerTask().execute(code);
    }
    private void onDataReceived(String result){
        result_message=result;

        if(result==null||result.length()<=0){
            //show error message
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context,
                    getString(R.string.error), duration);
            toast.show();
        }
        fillValues();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(TEXT_IMEI_CODE, imei_code);
        outState.putSerializable(TEXT_RES_MSG, result_message);
    }

    @Override
    protected void onPause() {
        removeDialog(PROGRESS_DIALOG);
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    private class checkerTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute() {
            showDialog(PROGRESS_DIALOG);
        }
        @Override
        protected String doInBackground(final String... args) {
            IMEIChecker chkr= new IMEIChecker();
            return chkr.CheckIMEI(args[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            dismissDialog(PROGRESS_DIALOG);
            onDataReceived(result);
        }
    }
}