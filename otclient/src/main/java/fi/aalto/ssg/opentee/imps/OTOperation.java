package fi.aalto.ssg.opentee.imps;

import java.util.ArrayList;
import java.util.List;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * OTOperation implements the ITEEClient.IOperation interface.
 */
public class OTOperation implements ITEEClient.IOperation {
    int started = 0;
    List<ITEEClient.IParameter> params = new ArrayList<>();

    /**
     * Public constructor with no Parameter.
     * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
     */
    public OTOperation(int started){
        this.started = started;
    }


    /**
     * Public constructor with 1 Parameter.
     * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
     * @param iParameter carry the parameters for this Operation.
     */
    public OTOperation(int started,
                     ITEEClient.IParameter iParameter){
        this.started = started;
        params.add(iParameter);
    }


    /**
     * Public constructor with 2 Parameters.
     * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
     * @param parameter1 carry the first parameters for this Operation.
     * @param parameter2 carry the second parameters for this Operation.
     */
    public OTOperation(int started,
                     ITEEClient.IParameter parameter1,
                     ITEEClient.IParameter parameter2){
        this.started = started;
        params.add(parameter1);
        params.add(parameter2);
    }


    /**
     * Public constructor with 3 Parameters.
     * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
     * @param parameter1 carry the first parameters for this Operation.
     * @param parameter2 carry the second parameters for this Operation.
     * @param parameter3 carry the third parameters for this Operation.
     */
    public OTOperation(int started,
                     ITEEClient.IParameter parameter1,
                     ITEEClient.IParameter parameter2,
                     ITEEClient.IParameter parameter3){
        this.started = started;
        params.add(parameter1);
        params.add(parameter2);
        params.add(parameter3);
    }


    /**
     * Public constructor with 4 Parameters.
     * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
     * @param parameter1 carry the first parameters for this Operation.
     * @param parameter2 carry the second parameters for this Operation.
     * @param parameter3 carry the third parameters for this Operation.
     * @param parameter4 carry the forth parameters for this Operation.
     */
    public OTOperation(int started,
                     ITEEClient.IParameter parameter1,
                     ITEEClient.IParameter parameter2,
                     ITEEClient.IParameter parameter3,
                     ITEEClient.IParameter parameter4){
        this.started = started;
        params.add(parameter1);
        params.add(parameter2);
        params.add(parameter3);
        params.add(parameter4);
    }

    public int getStarted(){
        return this.started;
    }

    public List<ITEEClient.IParameter> getParams(){
        return this.params;
    }

    public ITEEClient.IParameter getParam(int index){return this.params.get(index);}

    @Override
    public boolean isStarted() {
        return started != 0;
    }
}
