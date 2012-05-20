package ann;

import Utils.Config;
import Utils.Logger;
import data.DataLoader;
import data.DataProcessor;
import data.ImageProcessor;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.randomize.ConsistentRandomizer;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

/**
 *
 * @author Michal
 */
public class ANN implements Serializable{

    private double momentum = 0.3;
    private double learnRate = 0.7;
    private double errorRate = 0.01;
    boolean trained = false;
    private BasicNetwork network;
    private transient DataLoader loader;
    private transient DataProcessor processor;
    private transient ImageProcessor imageProcessor;
    private transient Logger logger; 
    
    ANN(DataLoader loader) {
        this.processor = loader.getDataProcessor();
        this.imageProcessor = loader.getImageProcessor();
        this.loader = loader;
        logger = new Logger();
    }

    public void train(TrainMethod method, boolean forceTraining) {
        
        if(!loader.isLoaded())
            loader.loadData(Config.dataPath);
                
        if (trained && !forceTraining) {
            logger.log("ANN is trained and ready to use");
            return;
        }
          
        final Propagation train;
        MLDataSet trainSet = loader.getTrainingSet();
        int input = trainSet.getInputSize();
        int output = trainSet.getIdealSize();
        int hidden = 100;
        
        if(network==null)
            getANN(input,hidden,output);
        
        new ConsistentRandomizer(-1, 1, 500).randomize(network);
        
        switch (method) {
            case ResilentPropagation: {
                logger.log("Training using ResilentPropagation");
                train = new ResilientPropagation(network, trainSet);
                break;
            }
            default:
            case BackPropagation: {
                logger.log("Training using BackPropagation");
                train = new Backpropagation(network, trainSet, learnRate, momentum);
                break;
            }
        }

        
        new Thread(new Runnable() {

            @Override
            public void run() {
                int epoch = 1;
                do {
                    train.iteration();
                    logger.log("Epoch " + epoch + " Error: " + train.getError());
                    epoch++;
                } while (train.getError() > errorRate);

                trained = true;
            }
        }).start();

    }



    private void getANN(int inputs, int hidden, int outputs) {

        network = new BasicNetwork();
        network.addLayer(new BasicLayer(null, true, inputs));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, hidden));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, outputs));
        network.getStructure().finalizeStructure();
        network.reset();
        trained = false;
    }

    public int getSubjectNbr(BufferedImage img) {

        throw new RuntimeException("Not supported");
    }

    public void setImageProcessor(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    public DataProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(DataProcessor processor) {
        this.processor = processor;
    }

    public ImageProcessor getImageProcessor() {
        return imageProcessor;
    }

    public enum TrainMethod {

        BackPropagation, ResilentPropagation
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public double getLearnRate() {
        return learnRate;
    }

    public void setLearnRate(double learnRate) {
        this.learnRate = learnRate;
    }

    public double getMomentum() {
        return momentum;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setLoader(DataLoader loader) {
        this.loader = loader;
        imageProcessor = loader.getImageProcessor();
        processor = loader.getDataProcessor();
    }

    
 
}