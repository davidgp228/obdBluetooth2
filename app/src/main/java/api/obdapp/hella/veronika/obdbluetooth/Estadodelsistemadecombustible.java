package api.obdapp.hella.veronika.obdbluetooth;

import com.github.pires.obd.commands.ObdCommand;

/**
 * Created by mac on 21/06/18.
 */

public class Estadodelsistemadecombustible extends ObdCommand {

    private double combustible= 0.00;

    public Estadodelsistemadecombustible() {
        super("01 03");
    }

    @Override
    protected void performCalculations() {
        int a = buffer.get(2);
        combustible = a;
    }

    @Override
    public String getFormattedResult() {
        return ""+combustible+"Litros";
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(combustible);
    }

    @Override
    public String getName() {
        return "Estado del sistema de combustible";
    }
}
