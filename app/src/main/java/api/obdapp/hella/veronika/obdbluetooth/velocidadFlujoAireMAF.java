package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 10/04/18.
 */

public class velocidadFlujoAireMAF extends ObdCommand {

    // Equivalent ratio (V)
    private double velocidad = 0.00;

    /**
     * Default ctor.
     */
    public velocidadFlujoAireMAF () {
        super("01 10");
    }


    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        int b = buffer.get(3);
        velocidad =  (a * 256 + b)/100;
    }

    @Override
    public String getFormattedResult() {
        return ""+velocidad+" ";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(velocidad);
    }

    @Override
    public String getName() {
        return "velocidad Flujo Aire MAF: Banco 1, Sensor 1";
    }
}
