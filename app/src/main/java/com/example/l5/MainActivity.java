package com.example.l5;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "mysettings";
    private boolean statePopup;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private Button btnDownload;
    private ImageButton btnDelete;
    private TextInputEditText idPdf;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        btnDelete = findViewById(R.id.btnDelete);
        btnDownload = findViewById(R.id.btnDownload);
        idPdf = findViewById(R.id.IdPdf);

        btnDelete.setActivated(false);
        //Инициализирование настроек приложения
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        statePopup = mSettings.getBoolean("statePopup", false);
        if(!statePopup) {
            showPopupWindow();
        }

        idPdf.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Инициализирование документа
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "journal" + idPdf.getText().toString() + ".pdf");

                //Проверка на существование документа
                if(idPdf.getText().toString().length() > 0 && file.exists()) {
                    btnDownload.setText("Посмотреть");
                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(file.getPath()));

                            startActivity(Intent.createChooser(intent, "dialogTitle"));

                        }
                    });

                    //Удаление файла
                    btnDelete.setActivated(true);
                    btnDelete.setVisibility(View.VISIBLE);
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                file.delete();
                                Toast.makeText(MainActivity.this, "Файл удалён", Toast.LENGTH_SHORT).show();

                                btnDownload.setText("Скачать PDF");

                                btnDelete.setActivated(false);
                                btnDelete.setVisibility(View.INVISIBLE);

                                idPdf.setText(null);
                            }
                        });
                }
                else {
                    btnDelete.setActivated(false);
                    btnDelete.setVisibility(View.INVISIBLE);

                    //Просмотр документа через браузер или установка его на устройство
                    btnDownload.setText("Скачать PDF");
                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Выбирете вариант загрузки");

                                builder.setNegativeButton("Загрузить на устройство", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        downloadPdf(idPdf.getText().toString());
                                        idPdf.setText(null);
                                    }
                                });

                                builder.setPositiveButton("Использовать браузер", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://ntv.ifmo.ru/file/journal/" +
                                                idPdf.getText().toString() + ".pdf")));
                                    }
                                });
                                builder.show();
                            } else {
                                idPdf.setText("Нет подключения к интернету");
                            }
                        }
                    });
                    }
                }
        });
    }

    //Загрузка документов с помощью менеджера установки Android
    private void downloadPdf(String IdPdf) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://ntv.ifmo.ru/file/journal/" + IdPdf + ".pdf"));
        request.setTitle("PDF Download");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "journal" + idPdf.getText().toString() + ".pdf");
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    //Инструкция для приложения
    private void showPopupWindow(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Использование приложения");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

        View addStudentWindow = inflater.inflate(R.layout.activity_show_popup_window, null);

        dialog.setView(addStudentWindow);

        CheckBox popupState = addStudentWindow.findViewById(R.id.popupState);

        dialog.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor = mSettings.edit();
                editor.putBoolean("statePopup", popupState.isChecked());
                editor.apply();
            }
        });

        dialog.show();
    }
}

