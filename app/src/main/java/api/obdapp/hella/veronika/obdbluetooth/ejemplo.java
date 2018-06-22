package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

/**
 * Created by mac on 07/04/18.
 */

public class ejemplo extends ObdCommand {

    // Equivalent ratio (V)
    private double oxigeno_voltage = 0.00;

    public ejemplo() {
        super("01 24");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        int b = buffer.get(3);
        oxigeno_voltage = (a * 256 + b) /32768;
    }

    @Override
    public String getFormattedResult() {
        return "Val= "+oxigeno_voltage;
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(oxigeno_voltage);
    }

    @Override
    public String getName() {
        return "Sensor de oxígeno 1";
    }
}
