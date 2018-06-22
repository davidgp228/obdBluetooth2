package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 21/06/18.
 */

public class Cargacalculadadelmotor extends ObdCommand {

    private double carga= 0.00;

    public Cargacalculadadelmotor() {
        super("01 04");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        carga = a/2.55;
    }

    @Override
    public String getFormattedResult() {
        return ""+carga+"%";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(carga);
    }

    @Override
    public String getName() {
        return "Carga calculada del motor ";
    }
}
