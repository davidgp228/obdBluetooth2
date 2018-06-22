package api.obdapp.hella.veronika.obdbluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;


public class MainActivity extends Activity {

    Button btnbluetooh, btniniciar, btnlocalizacion;
    TextView tverror, tvresultado, tvasync,tvcoordenadas,tvubicacion;
    boolean bandera = false, localizacion= false;

    BluetoothSocket socket;
    private BluetoothAdapter bluetoothAdapter;

    Context context;

    //***Localizacion
    LocationManager locationManager;
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;

    //** GET IMEI
    TelephonyManager telephonyManager;
    String deviceUniqueIdentifier = "";
    String latitud="";
    String longitud="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context= getApplication();

        btnbluetooh = findViewById(R.id.btnbluetooth);
        btniniciar = findViewById(R.id.btniniciar);
        btnlocalizacion= findViewById(R.id.btnlocalizacion);

        tverror = findViewById(R.id.tverror);
        tvresultado = findViewById(R.id.tvresult);
        tvasync = findViewById(R.id.tvaync);
        tvcoordenadas= findViewById(R.id.tvcoordenadas);
        tvubicacion= findViewById(R.id.tvubicacion);

        //**Verificar permiso para leer estado del telefono
        consultarIMEI();

        // Bluetooth se optiene dispositivo Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        conectarBluetooth();

        //** Localizacion
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnbluetooh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectarBluetooth();
            }
        });

        btnlocalizacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(localizacion==false) {

                        localizacion=true;

                        boolean gpsLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        boolean networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


                        if (networkLocationEnabled) {
                            btnlocalizacion.setBackgroundResource(R.drawable.locationblue);
                            networkUpdates();
                            Toast.makeText(getApplication(), "actualizacion Network", Toast.LENGTH_LONG).show();

                        }
                        else if (gpsLocationEnabled) {
                            btnlocalizacion.setBackgroundResource(R.drawable.locationblue);
                            GPSUpdates();
                            Toast.makeText(getApplication(), "actualizacion GPS", Toast.LENGTH_LONG).show();
                        }
                        else {
                            btnlocalizacion.setBackgroundResource(R.drawable.locationred);
                            tvubicacion.setText("GPS");
                            tvcoordenadas.setText("Error No disponible");
                        }

                    }else{

                        localizacion= false;

                        locationManager.removeUpdates(locationListenerNetwork);
                        locationManager.removeUpdates(locationListenerGPS);

                        btnlocalizacion.setBackgroundResource(R.drawable.locationred);
                        tvcoordenadas.setText("No disponible");

                    }

            }
        });


    }

    public synchronized void conectarBluetooth() {

        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, 1);
            tverror.setText("OBD no conectado ¡Bluetooth desactivado!");
            return;

        }

        tverror.setText("Esperando conexion...");

        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device.getAddress());
            }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = (String) devices.get(position);

                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    socket.connect();

                    if (socket.isConnected()) {
                        btniniciar.setBackgroundResource(R.drawable.inicioblue);
                        btniniciar.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.zoom));
                        btniniciar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (!bandera) {
                                    Toast.makeText(context, "Iniciando comandos", Toast.LENGTH_SHORT).show();
                                  //  btniniciar.startAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.rotate));
                                    bandera = true;

                                    //** Iniciar actualizaciones
                                     updateUI();

                                } else {
                                    Toast.makeText(context, "Comandos detenidos", Toast.LENGTH_SHORT).show();
                                    btniniciar.setBackgroundResource(R.drawable.pause);
                                    btniniciar.clearAnimation();
                                    bandera = false;
                                }
                            }
                        });

                        tverror.setText("Conexion correcta ** " + device.getName() + " **");

                    } else {
                        tverror.setText("No se pudo conectar al dispositivo OBD");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    tverror.setText("" + e);
                }

                // TODO save deviceAddress
            }
        });
        alertDialog.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.setTitle("Seleccionar dispositivo OBD II");
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void updateUI() {

        final updateUI updateUI = new updateUI();
        updateUI.getInstancia(socket);

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (bandera) {

                       /* runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    tvasync.setText(updateUI.getCommandOBD(socket));
                                }catch (Exception e){
                                    bandera= false;
                                    e.printStackTrace();
                                }
                            }
                        });
                        */
                        tvasync.post(new Runnable() {
                            public void run() {
                                tvasync.setText(" "+updateUI.getDataOBD());
                            }
                        });

                        Thread.sleep(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //******Tarea asincrona para actualizar la interfaz grafica
    ModuleVoltageCommand moduleVoltageCommand=null;
    EngineRPMObdCommand engineRpmCommand=null;
    SpeedObdCommand speedCommand=null;
    BarometricPressureCommand barometricPressureCommand=null;

    ejemplo ejemplo= null;
    Temperaturadelcatalizador temperaturadelcatalizador=null;
    Presiondelmedidordeltrendecombustible presiondelmedidordeltrendecombustible;
    valvulaEGR valvulaEGR;
    velocidadFlujoAireMAF velocidadFlujoAireMAF;

    private class actualizarInterfaz extends AsyncTask<Void, Integer, Void> {


        @Override
        protected void onPreExecute() {
            try{
                //** Unica instancia de cada clase
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());


                moduleVoltageCommand = new ModuleVoltageCommand();
                engineRpmCommand = new EngineRPMObdCommand();
                speedCommand = new SpeedObdCommand();
                BarometricPressureCommand barometricPressureCommand= new BarometricPressureCommand();

                ejemplo= new ejemplo();
                temperaturadelcatalizador= new Temperaturadelcatalizador();
                presiondelmedidordeltrendecombustible= new Presiondelmedidordeltrendecombustible();
                valvulaEGR= new valvulaEGR();
                velocidadFlujoAireMAF= new velocidadFlujoAireMAF();

            }catch (IOException e){
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        /*
        Ejecución del ordenamiento y transmision de progreso
         */
        @Override
        protected Void doInBackground(Void... params) {



            return null;
        }

        /*
         Se informa en progressLabel que se canceló la tarea y
         se hace invisile el botón "Cancelar"
          */
        @Override
        protected void onCancelled() {
            super.onCancelled();

        }

        /*
        Impresión del progreso en tiempo real
          */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        /*
        Se notifica que se completó el ordenamiento y se habilita
        de nuevo el botón "Ordenar"
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }


    }

    //******Localizacion>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    private boolean checkLocation() {
        Log.d("checkLocation ","value "+isLocationEnabled());
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {


        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void GPSUpdates() {

        if (!checkLocation())
            return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("GPS ACCESS","no permission");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1); // 1 is a integer which will return the result in onRequestPermissionsResult
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2 * 20 * 1000, 10, locationListenerGPS);
    }

    public void networkUpdates() {

        if (!checkLocation())
            return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Network ACCESS","no permission");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1); // 1 is a integer which will return the result in onRequestPermissionsResult
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 4000, 10, locationListenerNetwork);
    }


    private LocationListener locationListenerNetwork = new LocationListener() {

        public void onLocationChanged(Location location) {
            longitudeNetwork = location.getLongitude();
            latitudeNetwork = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvubicacion.setText("Network");
                    tvcoordenadas.setText("Longitud: "+ longitudeNetwork+" Latitud: "+latitudeNetwork);
                    latitud=""+latitudeNetwork ;
                    longitud=""+longitudeNetwork;
                    sendCoordenadas async= new sendCoordenadas();
                    async.execute();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("GPS onStatusChanged","Activated");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvubicacion.setText("Network");
                    tvcoordenadas.setText("Longitud: "+ longitudeNetwork+" Latitud: "+latitudeNetwork);
                }
            });

        }

        @Override
        public void onProviderEnabled(String s) {

        }
        @Override
        public void onProviderDisabled(String s) {

        }

    };

    private LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvubicacion.setText("GPS");
                    tvcoordenadas.setText("Longitud: "+ longitudeGPS+" Latitud: "+latitudeGPS);
                    Log.d("Run>>","Startted..");
                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                    latitud=""+latitudeGPS;
                    longitud=""+longitudeGPS;
                    sendCoordenadas async= new sendCoordenadas();
                    async.execute();
                }
            });
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("Network onStatusChanged","Activated");
        }

        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };

    //*** Mandar informacion a la base de datos
    public void consultarIMEI(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("READ_PHONE_STATE", "no permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 2);
            return;
        }
        else {
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            deviceUniqueIdentifier = tm.getDeviceId();
        }
    }

    public class sendCoordenadas extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            if(deviceUniqueIdentifier.equals("")){
                consultarIMEI();
            }

            try{

                URL url = new URL("http://albadti2018.ddns.net:8080/index.php?action=insertarPosicion");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("imei", deviceUniqueIdentifier);
                jsonParam.put("latitud", latitud);
                jsonParam.put("longitud", longitud);
                jsonParam.put("status", "0");

                Log.i("JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();
                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("MSG" , conn.getResponseMessage());

                conn.disconnect();
            }
            catch(Exception e){
                e.printStackTrace();
                return new String("Exception: " + e.getMessage());
            }

            return  null;
        }


    }



}
