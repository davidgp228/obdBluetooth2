package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 10/04/18.
 */

public class valvulaEGR extends ObdCommand {

    // Equivalent ratio (V)
    private double valvula = 0.00;

    /**
     * Default ctor.
     */
    public valvulaEGR () {
        super("01 2C");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        valvula = a/2.55;
    }

    @Override
    public String getFormattedResult() {
        return ""+valvula+" ";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(valvula);
    }

    @Override
    public String getName() {
        return "Valvula EGR: Banco 1, Sensor 1";
    }
}
