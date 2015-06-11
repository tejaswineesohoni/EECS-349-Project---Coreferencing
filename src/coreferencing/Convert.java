package coreferencing;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;
/**
 *
 * @author tejaswineesohoni
 */
public class Convert {
  /**
   * takes 2 arguments:
   * - CSV input file
   * - ARFF output file
   */
  public static void main(String[] args) throws Exception {
 
    // load CSV
    CSVLoader loader = new CSVLoader();
    loader.setSource(new File("coref_dataset_final.csv"));
    Instances data = loader.getDataSet();
 
    // save ARFF
    ArffSaver saver = new ArffSaver();
    saver.setInstances(data);
    saver.setFile(new File("coref_dataset_final.arff"));
    saver.setDestination(new File("coref_dataset_final.arff"));
    saver.writeBatch();
  }
}


