package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 21/06/18.
 */

public class Niveldeentradadeltanquedecombustible extends ObdCommand {

    private double combustible= 0.00;

    public Niveldeentradadeltanquedecombustible() {
        super("01 2F");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        combustible = a/2.55;
    }

    @Override
    public String getFormattedResult() {
        return ""+combustible+"%";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(combustible);
    }

    @Override
    public String getName() {
        return "Nivel de entrada del tanque de combustible";
    }
}
