package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 21/06/18.
 */

public class Temperaturadelliquidodeenfriamientodelmotor extends ObdCommand {

    private double temperatura= 0.00;

    public Temperaturadelliquidodeenfriamientodelmotor() {
        super("01 05");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        temperatura = a-40;
    }

    @Override
    public String getFormattedResult() {
        return ""+temperatura+"°C";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(temperatura);
    }

    @Override
    public String getName() {
        return "Temperatura del líquido de enfriamiento del motor";
    }
}
