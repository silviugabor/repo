package repo.spark.service.repository;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.Normalizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import repo.spark.transformers.ByteArrayToString;
import repo.spark.transformers.Cleanser;
import repo.spark.transformers.Exploder;
import repo.spark.transformers.Stemmer;
import repo.spark.transformers.Uniter;

/**
 * This class is used because loading the model from parquet exhibits problems. Currently looking for a solution.
 * @author SilviuG
 *
 */
//@Configuration
@SuppressWarnings("unused")
public class PipelineConfiguration {
	
	private static final String JDBC_FORMAT = "jdbc";
	private static final String JDBC_URL = "url";
	private static final String JDBC_DBTABLE = "dbtable";
	private static final String JDBC_USER = "user";
	private static final String JDBC_PASSWORD = "password";

	@Value("${repository.jdbc.url}")
    private String jdbcURL;

    @Value("${repository.jdbc.dbtable}")
    private String jdbcDBTable;

    @Value("${repository.jdbc.user}")
    private String jdbcUser;

    @Value("${repository.jdbc.password}")
    private String jdbcPassword;

	@Autowired
	private SparkSession sparkSession;


	private Dataset<Row> readJDBCTrainingSet() {
		Dataset<Row> jdbcTrainingSet = sparkSession.read()
				.format(JDBC_FORMAT)
				.option(JDBC_URL, jdbcURL)
				.option(JDBC_DBTABLE, jdbcDBTable)
				.option(JDBC_USER, jdbcUser)
				.option(JDBC_PASSWORD, jdbcPassword)
				.load();

		jdbcTrainingSet = jdbcTrainingSet.select("id", "description");
		jdbcTrainingSet.count();
		jdbcTrainingSet.cache();

		return jdbcTrainingSet;
	}
	
	@Bean
	public PipelineModel pipelineFeatureExtractionModel(){
		Dataset<Row> trainingSet = readJDBCTrainingSet();
		
		ByteArrayToString byteArrayToString = new ByteArrayToString().setInputId("id").setInputColumn("description").setOutputColumn("string_description");
		Cleanser cleanser = new Cleanser().setInputColumn(byteArrayToString.getOutputColumn()).setOutputColumn("cleaned_description");
		Tokenizer tokenizer = new Tokenizer().setInputCol(cleanser.getOutputColumn()).setOutputCol("words");
		StopWordsRemover stopWordsRemover = new StopWordsRemover().setInputCol(tokenizer.getOutputCol()).setOutputCol("removed_stop_words").setCaseSensitive(false);
		Exploder exploder = new Exploder().setInputId("id").setInputColumn(stopWordsRemover.getOutputCol()).setOutputColumn("singulars");
		Stemmer stemmer = new Stemmer().setInputId("id").setInputColumn(exploder.getOutputColumn()).setOutputColumn("stemmed_word");
		Uniter uniter = new Uniter().setInputId("id").setInputColumn(stemmer.getOutputColumn()).setOutputColumn("stemmed_word_list");
		//Word2Vec w2v = new Word2Vec().setInputCol("stemmed_word_list").setOutputCol("final_features").setWindowSize(7).setVectorSize(700).setSeed(1L);
		//PCA pca = new PCA().setInputCol("w2v_features").setOutputCol("final_features").setK(500);
		HashingTF hashingTF = new HashingTF().setInputCol(uniter.getOutputColumn()).setOutputCol("tf_features").setNumFeatures(2000);
		IDF idf = new IDF().setInputCol(hashingTF.getOutputCol()).setOutputCol("idf_features").setMinDocFreq(3);
		Normalizer normalizer2 = new Normalizer().setP(2).setInputCol(idf.getOutputCol()).setOutputCol("final_scaled_features");
//		
//		Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{
//				byteArrayToString,
//				cleanser, 
//				tokenizer, 
//				stopWordsRemover, 
//				exploder, 
//				stemmer, 
//				uniter,
//				hashingTF,
//				idf,
//				normalizer2
//		});
//		PipelineModel pipelineModel = pipeline.fit(trainingSet);
//		
//		//start of Elbow method algorithm to determine optimum number of clusters
//		List<Row> originalTrainingSetList = trainingSet.collectAsList();
//		Dataset<Row> transformedTrainingSet = pipelineModel.transform(trainingSet);
//		double WSSSE_prec = Double.MAX_VALUE;
		//PipelineModel previousPipelineModel = null;
//		KMeans previousKMeans = new KMeans();
		//KMeansModel previousKMeansModel = null;
		//int i = 2;
		/*while (true){
			KMeans kmeans = new KMeans().setK(i).setSeed(1).setFeaturesCol("final_scaled_features");
			//pipeline.setStages(new PipelineStage[]{kmeans});
			//pipelineModel = pipeline.fit(transformedTrainingSet);
			//KMeansModel kmeansModel = (KMeansModel) pipelineModel.stages()[0];
			KMeansModel kmeansModel = kmeans.fit(transformedTrainingSet);
			kmeansModel.setFeaturesCol("final_scaled_features").computeCost(transformedTrainingSet);
			double WSSSE = kmeansModel.setFeaturesCol("final_scaled_features").computeCost(transformedTrainingSet);
			if (WSSSE > WSSSE_prec)
				break;
			WSSSE_prec = WSSSE;
			previousKMeans = kmeans;
			//previousPipelineModel = pipelineModel;
			previousKMeansModel = kmeansModel;
		}*/
		
//		previousKMeans.setK(30).setSeed(1L).setFeaturesCol("final_scaled_features");
//		previousKMeansModel = previousKMeans.fit(transformedTrainingSet);
//		
//		//KMeansModel previousKMeansModel = (KMeansModel) previousPipelineModel.stages()[0];
//		List<Row> transformedTrainingSetList = transformedTrainingSet.collectAsList();
//		Map<Integer, List<Row>> map = new HashMap<>();
//		for (i = 0 ; i < originalTrainingSetList.size() - 1; i++){
//			int cluster = previousKMeansModel.predict(transformedTrainingSetList.get(i).getAs("final_scaled_features"));
//			if (map.containsKey(cluster))
//				map.get(cluster).add(originalTrainingSetList.get(i));
//			else
//			{
//				map.put(cluster, new ArrayList<>());
//				map.get(cluster).add(originalTrainingSetList.get(i));
//			}
//		}
		
//		Pipeline pipeline = new Pipeline();
//		pipeline.setStages(new PipelineStage[]{
//				byteArrayToString,
//				cleanser, 
//				tokenizer,
//				stopWordsRemover,
//				exploder, 
//				stemmer, 
//				uniter,
//				hashingTF,
//				idf,
//				normalizer2
//		});
//		PipelineModel previousPipelineModel = pipeline.fit(trainingSet);
		/*map.forEach((clusterIndex, list) -> {
			list.forEach((row) -> {
				LOG.info("K-Means :: " + "Cluster " + clusterIndex + ":" + row.getAs("description"));
			});
		});*/
		
		Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{
				byteArrayToString,
				cleanser, 
				tokenizer, 
				stopWordsRemover,
				exploder, 
				stemmer, 
				uniter,
				hashingTF,
				idf,
				normalizer2
		});
		PipelineModel pipelineModel = pipeline.fit(trainingSet);

		
		return pipelineModel;
	}

}
