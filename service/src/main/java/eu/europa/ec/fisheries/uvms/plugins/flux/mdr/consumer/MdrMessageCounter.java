package eu.europa.ec.fisheries.uvms.plugins.flux.mdr.consumer;

/**
 * Created by kovian on 08/07/2016.
 */
public class MdrMessageCounter {

    private static int i;

    public static void increment(){
        i++;
    }

    public int getI() {
        return i;
    }
}
