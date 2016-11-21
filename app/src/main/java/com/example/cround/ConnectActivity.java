package com.example.cround;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int BLUETOOTH_PERMISSION_CODE = 0;
    private static final int BLUETOOTH_CODE = 1;
    private static final String ARDUINO_MAC = "20:16:05:30:56:26";
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String uriPath1 = "android.resource://com.example.cround/"+R.raw.video_1;
    private static final String uriPath2 = "android.resource://com.example.cround/"+R.raw.video_2;
    private static final String uriPath3 = "android.resource://com.example.cround/"+R.raw.video_3;
    private static final String[] channels = {"CH01", "CH02", "CH03"};
    private static final int UP_CHANNEL_FLAG = 1;
    private static final int DOWN_CHANNEL_FLAG = 2;
    private static final int HANDLER_STATE = 0;
    private static final char SUBIR_CANAL_OP = '1';
    private static final char BAJAR_CANAL_OP = '2';
    private static final char SUBIR_VOLUMEN_OP = '3';
    private static final char BAJAR_VOLUMEN_OP = '4';
    private static final char MUTE_OP = '5';
    private static final int RETARDO = 200;

    private AudioManager am;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket btSocket;
    private StringBuilder recDataString = new StringBuilder();
    Handler bluetoothIn;

    Button btnConnect, btnIr;
    VideoView videoView;
    TextView channel, up_vol, down_vol, tvMute;

    private Uri[] uri;
    private int i = 0;
    private boolean modoTV = false;
    private boolean mute = false;

    private ConnectedThread mConnectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        modoTV = false;

        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(this);
        btnIr = (Button) findViewById(R.id.btn_ir_tv);

        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        uri = new Uri[]{Uri.parse(uriPath1), Uri.parse(uriPath2), Uri.parse(uriPath3)};

        CheckBluetoothPermissions();

        bluetoothIn = new Handler() {
            public void handleMessage(Message msg) {
                try {

                    if (msg.what == HANDLER_STATE) {      //Si el estado del mensaje es 0 es del bluetooth
                        String readMessage = (String) msg.obj;           //Se obtiene el cuerpo del mensaje
                        recDataString.append(readMessage);     //Adjunta los caracteres al string hasta encontrar el caracter + (delimitador)
                        int endOfLineIndex = recDataString.indexOf("+");  //Obtiene la posicion del delimitador
                        if (endOfLineIndex > 0) {                         // verifica que la carga del mensaje tenga mas de un caracter
                            String dataInPrint = recDataString.substring(0, endOfLineIndex);    // Extrae el string
                            int dataLength = dataInPrint.length();       // Obtiene el tamaño del mensaje
                            /**** LOGs de mensaje y tamaño del mensaje ****/
                            Log.e("Datos recibidos = ", dataInPrint);
                            Log.e("Tamaño del String = ", String.valueOf(dataLength));
                            if (modoTV) {
                                Log.e("modo", "true");
                                switch (recDataString.charAt(0)) {  //Opciones segun el mensaje recibido
                                    case SUBIR_CANAL_OP:
                                        ChangeChannel(UP_CHANNEL_FLAG);
                                        Thread.sleep(RETARDO);
                                        break;
                                    case BAJAR_CANAL_OP:
                                        ChangeChannel(DOWN_CHANNEL_FLAG);
                                        Thread.sleep(RETARDO);
                                        break;
                                    case SUBIR_VOLUMEN_OP:
                                        RaiseVolume();
                                        Thread.sleep(RETARDO);
                                        break;
                                    case BAJAR_VOLUMEN_OP:
                                        LowerVolume();
                                        Thread.sleep(RETARDO);
                                        break;
                                    case MUTE_OP:
                                        Mute();
                                        Thread.sleep(RETARDO);
                                        break;

                                }
                            }

                            recDataString.delete(0, recDataString.length()); //Limpiar el  buffer del mensaje
                        }
                    }

                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };


    }

    /** Solicitud de permisos de Bluetooth **/
    private void CheckBluetoothPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_CODE);
        } else {
            CheckBluetoothEnable();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == BLUETOOTH_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CheckBluetoothEnable();
            } else {
                CheckBluetoothPermissions();
            }
        }
    }


    /** Verifica si ya se encendio el GPS y el acceso a Internet **/
    private void CheckBluetoothEnable() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("ALVIS", "sin permisos");
            Toast.makeText(this, "No es posible acceder al Bluetooth en este dispositivo", Toast.LENGTH_LONG).show();
            finish();
        }

        if(!mBluetoothAdapter.isEnabled()) {
            ShowAlert();
        } else {
            ConnectionInit();
        }
    }

    private void ShowAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("CROUND");
        builder.setMessage("El Bluetooth esa apagado.\n¿Desea activarlo?");
        builder.setCancelable(false);
        builder.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, BLUETOOTH_CODE);
                    //ConnectActivity.this.startActivityForResult(new Intent(Settings.ACTION_SETTINGS), BLUETOOTH_CODE);
            }
        });
        builder.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ConnectActivity.this.finish();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("alvis", String.valueOf(resultCode));
        if (requestCode ==  BLUETOOTH_CODE &&  mBluetoothAdapter.isEnabled()) {
            btnConnect.setOnClickListener(this);
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BLUETOOTH_CODE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBluetoothAdapter != null && btSocket != null && btSocket.isConnected()) {
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void ConnectionInit() {
            device = mBluetoothAdapter.getRemoteDevice(ARDUINO_MAC);
            try {
                if(btSocket==null || !btSocket.isConnected())
                    btSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID);

                if (btSocket.isConnected())
                    btSocket.close();
                else
                    btSocket.connect();

            } catch (IOException e) {
                Toast.makeText(this, "No se pudo establecer la conexión", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                try {
                    btnConnect.setText("CONECTAR");
                    btSocket.close();
                } catch (IOException e2) {
                    //insert code to deal with this
                }
            }



        if (btSocket != null) {
            if (btSocket.isConnected()) {
                Toast.makeText(this, "Conectado", Toast.LENGTH_SHORT).show();
                btnConnect.setText("DESCONECTAR");
                btnIr.setOnClickListener(this);

                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();

            } else {
                btnConnect.setText("CONECTAR");
                btnIr.setOnClickListener(null);
            }
        }


    }

    private void GoToTV() {
        setContentView(R.layout.content_tv);

        videoView = (VideoView) findViewById(R.id.video_view);
        channel = (TextView) findViewById(R.id.tv_channel);
        channel.setText(channels[0]);
        channel.setOnClickListener(this);
        up_vol = (TextView) findViewById(R.id.tv_up_volume);
        up_vol.setOnClickListener(this);
        down_vol = (TextView) findViewById(R.id.tv_down_volume);
        down_vol.setOnClickListener(this);
        tvMute = (TextView) findViewById(R.id.tv_mute);
        tvMute.setOnClickListener(this);


        //videoView.hori
        videoView.setVideoURI(uri[0]);
        videoView.requestFocus();
        videoView.start();

        modoTV = true;
    }


    public void ChangeChannel(int flag) {
        if(flag == UP_CHANNEL_FLAG) {
            i = (i + 1) % 3;
        } else if(flag == DOWN_CHANNEL_FLAG) {
            i = (i + 2) % 3;
        }
        Uri uriCh = uri[i];
        videoView.setVideoURI(uriCh);
        videoView.start();
        channel.setText(channels[i]);
    }


    public void RaiseVolume() {
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
    }

    public void LowerVolume() {
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
    }

    public void Mute() {
        mute = !mute;
        if(mute) {
            CancelVolume();
            return;
        } else {
            EnableVolume();
            return;
        }
    }

    public void EnableVolume() {
        LowerVolume();LowerVolume();LowerVolume();LowerVolume();
        LowerVolume();LowerVolume();LowerVolume();LowerVolume();
    }

    public void CancelVolume() {
        RaiseVolume();RaiseVolume();RaiseVolume();RaiseVolume();
        RaiseVolume();RaiseVolume();RaiseVolume();RaiseVolume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_connect:
                if (device == null) {
                    CheckBluetoothPermissions();
                } else {
                    ConnectionInit();
                }
                break;

            case R.id.btn_ir_tv:
                GoToTV();
                break;

            case R.id.tv_channel:
                ChangeChannel(UP_CHANNEL_FLAG);
                break;

            case R.id.tv_up_volume:
                RaiseVolume();
                break;

            case R.id.tv_down_volume:
                LowerVolume();
                break;

            case R.id.tv_mute:
                Mute();
                break;
        }

    }


    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(HANDLER_STATE, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


}
