package api.obdapp.hella.veronika.obdbluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.pressure.BarometricPressureCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.net.Socket;


/**
 * Created by mac on 09/04/18.
 */

public class updateUI {

    //**Comando AT
    ModuleVoltageCommand moduleVoltageCommand=null;
    SpeedCommand speedCommand=null;
    BarometricPressureCommand barometricPressureCommand=null;

    ejemplo ejemplo= null;
    Temperaturadelcatalizador temperaturadelcatalizador=null;
    Presiondelmedidordeltrendecombustible presiondelmedidordeltrendecombustible;
    valvulaEGR valvulaEGR;
    velocidadFlujoAireMAF velocidadFlujoAireMAF;
    Cargacalculadadelmotor cargacalculadadelmotor;
    Temperaturadelliquidodeenfriamientodelmotor temperaturadelliquidodeenfriamientodelmotor;

    //** socket
    BluetoothSocket socket=null;

    public void getInstancia(BluetoothSocket socketa){

        if(socket==null) {
            try {
                this.socket= socketa;
                //** Unica instancia de cada clase
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());


                moduleVoltageCommand = new ModuleVoltageCommand();
                speedCommand = new SpeedCommand();
                barometricPressureCommand= new BarometricPressureCommand();

                ejemplo = new ejemplo();
                temperaturadelcatalizador = new Temperaturadelcatalizador();
                presiondelmedidordeltrendecombustible = new Presiondelmedidordeltrendecombustible();
                valvulaEGR = new valvulaEGR();
                velocidadFlujoAireMAF = new velocidadFlujoAireMAF();
                cargacalculadadelmotor= new Cargacalculadadelmotor();
                temperaturadelliquidodeenfriamientodelmotor= new Temperaturadelliquidodeenfriamientodelmotor();


            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public String getDataOBD(){

        String cadena="";
        try {

            //**Consultas a O-bd ---------------------
            try{
                presiondelmedidordeltrendecombustible.run(socket.getInputStream(), socket.getOutputStream());
                cadena+="Presion del medidor del trende combustible: " +presiondelmedidordeltrendecombustible.getFormattedResult()+"\n\n";
            }catch (Exception e){
                cadena+="Presion del medidor del trende combustible: NO DATA\n" +
                        "Recomendacion: Condiciones que afectan al sensor FRP; filtro de combustible obstruido," +
                        "combustible sucio o lectura erronea de la señal.\n\n";
            }

            try{
                temperaturadelcatalizador.run(socket.getInputStream(), socket.getOutputStream());
                cadena+="Temperatura del catalizador: " +temperaturadelcatalizador.getFormattedResult()+"\n\n";
            }catch (Exception e){
                cadena+="Temperatura del catalizador: NO DATA\n" +
                        "Recomendacion:  Ruido y vibraciones en la linea de escape, falta de potencia al acelarar y" +
                        " RPM bajas en realenty.\n\n";
            }
            try{
                barometricPressureCommand.run(socket.getInputStream(), socket.getOutputStream());
                cadena+="Presion barometrica: " +barometricPressureCommand.getFormattedResult()+"\n\n";
            }catch (Exception e){
                cadena+="Presion barometrica: NO DATA\n" +
                        "Recomendacion: Chacar presion del riel de combustible.\n\n";
            }

            try{
                ejemplo.run(socket.getInputStream(), socket.getOutputStream());
                cadena+="Sensor de oxígeno: " +ejemplo.getFormattedResult()+"\n\n";
            }catch (Exception e){
                Log.d("mensaje", "Sensor de oxígeno: NO DATA");
                cadena+="Sensor de oxígeno: NO DATA\n" +
                        "Recomendacion: Checar voltaje del sensor de oxigeno.\n\n";
            }

            try{
                moduleVoltageCommand.run(socket.getInputStream(), socket.getOutputStream());
                cadena+="Modulo de voltaje: " +moduleVoltageCommand.getFormattedResult()+"\n\n";
            }catch (Exception e){
                cadena+="Modulo de voltaje: NO DATA\n" +
                        "Recomendacion: Checar voltaje de la bateria, reemplazar bateria.\n\n";
            }

            try{
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                Log.d("mensaje", "speedCommand: " +speedCommand.getFormattedResult());
                cadena+="Velocimetro: " +speedCommand.getFormattedResult()+"\n\n";
            }catch (Exception e){
                cadena+="Velocimetro: NO DATA\n" +
                        "Recomendacion: Checar sensor del velocimentro.\n\n";
            }

            try{
                valvulaEGR.run(socket.getInputStream(), socket.getOutputStream());
                cadena+="Valvula EGR: " +valvulaEGR.getFormattedResult()+"\n\n";
            }catch (Exception e){
                cadena+="valvulaEGR: NO DATA\n" +
                        "Recomendacion:Resta potencia al motor, tirones o dificultad de arranque en frio. Retirar valvula EGR " +
                        "y limpiar los puertos de entrada y salidad con carbuclean. O en su defecto reemplzar .\n\n";
            }

            try{
                velocidadFlujoAireMAF.run(socket.getInputStream(), socket.getOutputStream());
                cadena+="Velocidad Flujo Aire MAF: " +velocidadFlujoAireMAF.getFormattedResult()+"\n\n";
            }catch (Exception e){
                e.printStackTrace();
                cadena+="Velocidad Flujo Aire MAF: NO DATA\n" +
                        "Recomendacion: revisar mangueras o conectaros unidos al cuerpo de aceleracion o reemplazar sensor de flujo de aire MAF" +
                        "y revisar filtro de aire que este bien instalado.\n\n";
            }
            try{
                cargacalculadadelmotor.run(socket.getInputStream(), socket.getOutputStream());
                cadena+=cargacalculadadelmotor.getName() +cargacalculadadelmotor.getFormattedResult()+"\n\n";
            }catch (Exception e){
                e.printStackTrace();
                cadena+=cargacalculadadelmotor.getName()+": NO DATA\n" +
                        "Recomendacion: \n\n";
            }

            try{
                temperaturadelliquidodeenfriamientodelmotor.run(socket.getInputStream(), socket.getOutputStream());
                cadena+=temperaturadelliquidodeenfriamientodelmotor.getName() +temperaturadelliquidodeenfriamientodelmotor.getFormattedResult()+"\n\n";
            }catch (Exception e){
                e.printStackTrace();
                cadena+=temperaturadelliquidodeenfriamientodelmotor.getName()+": NO DATA\n" +
                        "Recomendacion: \n\n";
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
        return cadena;
    }
}
