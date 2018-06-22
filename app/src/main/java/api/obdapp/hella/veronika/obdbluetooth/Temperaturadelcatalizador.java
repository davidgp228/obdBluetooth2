package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 07/04/18.
 */

public class Temperaturadelcatalizador extends ObdCommand {
    // Equivalent ratio (V)
    private double catalizador = 0.00;

    /**
     * Default ctor.
     */
    public Temperaturadelcatalizador() {
        super("01 3C");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        int b = buffer.get(3);
        catalizador = (a * 256 + b) /10-40;
    }

    @Override
    public String getFormattedResult() {
        return " "+catalizador+"° c";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(catalizador);
    }

    @Override
    public String getName() {
        return "\tTemperatura del catalizador: Banco 1, Sensor 1";
    }
}
