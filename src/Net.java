import java.util.ArrayList;
import java.util.List;

public class Net {

    List<Layer> layers = new ArrayList<>(); // m_layers.get(layersNum).getLayer().get(neuronNum) pour un neuronne donné
    double error;



    double recentAverageError;
    double recentAverageSmoothingFactor;

    public Net(List<Integer> topology) {
        int numLayers = topology.size();
        for (int layerNum = 0; layerNum < numLayers; ++layerNum)
        {
            Layer tmpLayer = new Layer();

            //the number of output connection is equal to the number of neuron of the next layer
            //We handle the case of the last layer with this "French Ternaire"
            int numOutputs = layerNum == topology.size() - 1 ? 0 : topology.get(layerNum + 1);

            //we made a new layer, now fill it with neurons, and add
            //a bias neuron to the layer
            for (int neuronNum = 0; neuronNum <= topology.get(layerNum); ++neuronNum)
            {
                tmpLayer.AddNeuron(new Neuron(numOutputs, neuronNum));
            }
            layers.add(tmpLayer);
            // Force the bias node's output value to 1.0 , it is the last neuron created above
            layers.get(layers.size()-1).getLayer().get(layers.get(layers.size()-1).getLayer().size()-1).SetOutputVal(1.0);
            //Layers.back().back().SetOutputVal(1.0); c++ version
        }
    }

    List<Layer> GetNet() { return layers; }
    int GetNBLayers() { return GetNet().size(); }

    public double GetRecentAverageError() {
        return recentAverageError;
    }

    void FeedForward(List<Double> inputVals)
    {
        //check if the number of input values fit the number of input neurons
        //(counting the bias neuron)(m_Layers.size()-1)
        assert(inputVals.size() == layers.get(0).getLayer().size() - 1);

        for (int i = 0; i < inputVals.size(); ++i)
        {
            layers.get(0).getLayer().get(i).SetOutputVal(inputVals.get(i));
        }

        //forward propagate
        //the aim of this step is to pass to each neuron of all the layer his input
        //and transform the output in new input for the next layer of neurons
        for (int layerNum = 1; layerNum < layers.size(); ++layerNum)
        {
            Layer prevLayer = layers.get(layerNum - 1);
            for (int n = 0; n < layers.get(layerNum).getLayer().size() - 1; ++n)
            {
                layers.get(layerNum).getLayer().get(n).FeedForward(prevLayer);
            }
        }
    }


    void BackProp(List<Double> targetVals)
    {
        // Calculate overall net error (RMS (root mean square) of output neuron error)
        //rms = sqrt((1/n(sum(i->1->n)(target(i)-actual(i))²)
        Layer outputLayer = layers.get(layers.size()-1);
        error = 0.0;

        for (int n = 0; n < outputLayer.getLayer().size() - 1; ++n)
        {
            double delta = targetVals.get(n) - outputLayer.getLayer().get(n).GetOutputVal();
            error += delta * delta;
        }
        error /= outputLayer.getLayer().size() - 1; // get the average error squared
        error = Math.sqrt(error); // RMS

        // Implement a recent average measurement of the precedent error
        recentAverageError = (recentAverageError * recentAverageSmoothingFactor + error)
                / (recentAverageSmoothingFactor + 1.0);


        // Calculate output layer gradients

        for (int n = 0; n < outputLayer.getLayer().size() - 1; ++n)
        {
            outputLayer.getLayer().get(n).CalcOutputGradients(targetVals.get(n));
        }

        // Calculate gradient on hidden layers

        for (int layerNum = layers.size() -2; layerNum > 0; --layerNum)
        {
            Layer hiddenLayer = layers.get(layerNum);
            Layer nextLayer = layers.get(layerNum + 1);

            for(int n = 0; n < hiddenLayer.getLayer().size(); ++n)
            {
                hiddenLayer.getLayer().get(n).CalcHiddenGradients(nextLayer);
            }
        }

        // For all layers from ouputs to first hidden layer
        // Update the connection weights

        for (int layerNum = layers.size() - 1; layerNum > 0; --layerNum)
        {
            Layer layer = layers.get(layerNum);
            Layer prevLayer = layers.get(layerNum - 1);

            for (int n = 0; n < layer.getLayer().size() - 1; ++n)
            {
                layer.getLayer().get(n).UpdateInputWeights(prevLayer);
            }
        }
    }


    List<Double> GetResult(){
        List<Double> resultVals = new ArrayList<>();
        for (int n = 0; n < layers.get(layers.size()-1).getLayer().size() - 1; ++n)
        {
            resultVals.add(layers.get(layers.size()-1).getLayer().get(n).GetOutputVal());
        }
        return resultVals;
    }


    @Override
    public String toString(){
        String res = "";
        for(int i =0; i<layers.size();i++){
            res +="Layer :" + i +"\n";
            for(int j=0; j<layers.get(i).getLayer().size();j++){
                res +="Neuron " + j +" is " +layers.get(i).getLayer().get(j)+"\n";
            }
        }
        return res;
    }
}
