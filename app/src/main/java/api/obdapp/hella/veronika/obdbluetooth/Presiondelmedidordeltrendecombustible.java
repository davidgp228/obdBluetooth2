package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 07/04/18.
 */

public class Presiondelmedidordeltrendecombustible extends ObdCommand {

    // Equivalent ratio (V)
    private double presion = 0.00;

    /**
     * Default ctor.
     */
    public Presiondelmedidordeltrendecombustible() {
        super("01 23");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        int b = buffer.get(3);
        presion = 10*(a * 256 + b);
    }

    @Override
    public String getFormattedResult() {
        return ""+presion+" ";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(presion);
    }

    @Override
    public String getName() {
        return "Temperatura del catalizador: Banco 1, Sensor 1";
    }
}
