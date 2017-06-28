package repo.spark.service;

import java.io.IOException;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.clustering.BisectingKMeans;
import org.apache.spark.ml.clustering.BisectingKMeansModel;
import org.apache.spark.ml.clustering.GaussianMixture;
import org.apache.spark.ml.clustering.GaussianMixtureModel;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.util.MLWritable;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import scala.Tuple2;

@Component
public class MLService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MLService.class);
	
	@Autowired
	private SparkSession sparkSession;

	//Methods to train unsupervised learning
	public KMeansModel trainKMeans(String fullSetParquetFilePath, Integer numberOfClusters) {
		Dataset<Row> dataset = getTrainingDataset(fullSetParquetFilePath);
		KMeans kmeansAlgorithm = new KMeans().setK(numberOfClusters);
		return kmeansAlgorithm.fit(dataset);
	}

	public GaussianMixtureModel trainGaussianMixture(String fullSetParquetFilePath, Integer numberOfClusters) {
		Dataset<Row> dataset = getTrainingDataset(fullSetParquetFilePath);
		GaussianMixture gaussianMixture = new GaussianMixture().setK(numberOfClusters);
		return gaussianMixture.fit(dataset);
	}

	public LDAModel trainLatentDirichletAllocation(String fullSetParquetFilePath, Integer numberOfClusters) {
		Dataset<Row> dataset = getTrainingDataset(fullSetParquetFilePath);
		LDA latentDirichletAllocation = new LDA().setK(numberOfClusters);
		return latentDirichletAllocation.fit(dataset);
	}

	public BisectingKMeansModel trainBisectingKMeans(String fullSetParquetFilePath, Integer numberOfClusters) {
		Dataset<Row> dataset = getTrainingDataset(fullSetParquetFilePath);
		BisectingKMeans bisectingKMeans = new BisectingKMeans().setK(numberOfClusters);
		return bisectingKMeans.fit(dataset);
	}

	@SuppressWarnings("unused")
	private Tuple2<Dataset<Row>, Dataset<Row>> getTrainingAndTestDatasets(final String fullSetParquetFilePath) {
		Dataset<Row> fullSetDataFrame = sparkSession.read().parquet(fullSetParquetFilePath);

		// Split initial RDD into two... [70% training data, 30% testing data].
		Dataset<Row>[] splitDataFrames = fullSetDataFrame.randomSplit(new double[] { 0.7D, 0.3D });
		splitDataFrames[0].cache();
		splitDataFrames[0].count();

		return new Tuple2<>(splitDataFrames[0], splitDataFrames[1]);
	}

	private Dataset<Row> getTrainingDataset(final String fullSetParquetFilePath) {
		Dataset<Row> dataset = sparkSession.read().parquet(fullSetParquetFilePath);
		dataset.cache();
		dataset.count();
		return dataset;
	}

	public <T extends MLWritable> void saveModel(T model, String modelOutputDirectory) {
		try {
			model.write().overwrite().save(modelOutputDirectory);

			LOG.info("\n------------------------------------------------");
			LOG.info("Saved model to " + modelOutputDirectory);
			LOG.info("------------------------------------------------\n");
		} catch (IOException e) {
			throw new RuntimeException(String.format("Exception occurred while saving the model to disk. Details: %s", e.getMessage()), e);
		}
	}
	
	public KMeansModel loadKMeansModel(String modelDirectory){
		KMeansModel model = KMeansModel.load(modelDirectory);
		return model;
	}
	
	
	public void saveKMeansModel(KMeansModel model, String modelOutputDirectory){
		try{
			model.write().overwrite().save(modelOutputDirectory);
		}
		catch(IOException e){
			throw new RuntimeException(String.format("Exception occurred while saving the model to disk. Details: %s", e.getMessage()), e);
		}
	}

	public PipelineModel loadPipelineModel(String modelDirectory) {
		PipelineModel model = PipelineModel.load(modelDirectory);

		LOG.info("\n------------------------------------------------");
		LOG.info("Loaded pipeline model from " + modelDirectory);
		LOG.info("------------------------------------------------\n");

		return model;
	}
}
