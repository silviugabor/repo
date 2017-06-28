package repo.spark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import repo.spark.transformers.Cleanser;
import repo.spark.transformers.Exploder;
import repo.spark.transformers.Stemmer;
import repo.spark.transformers.Uniter;


public class Main {
	
	private static SparkSession sparkSession = SparkSession.builder()
            .master("local[*]")
            .appName("Simple App")
            .config("spark.cores.max", "4")
            .config("spark.driver.memory", "2g")
            .config("spark.executor.memory", "2g")
            .getOrCreate();;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());
		List<Row> data = Arrays.asList(RowFactory.create(0.0, "Hi I heard about Spark"),
				RowFactory.create(0.0, "I wish Java could use case classes"),
				RowFactory.create(1.0, "Logistic regression models are neat"));
		StructType schema = new StructType(
				new StructField[] { new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
						new StructField("sentence", DataTypes.StringType, false, Metadata.empty()) });
		Dataset<Row> jdbcDF = sparkSession.read()
			      .format("jdbc")
			      .option("url", "jdbc:mysql://localhost:3306/licenta?unicode=true&characterEncoding=utf-8&autoReconnect=true&useSSL=false")
			      .option("dbtable", "licenta.repository")
			      .option("user", "crow")
			      .option("password", "crow")
			.load();
		JavaRDD<Row> rddrow = jsc.parallelize(data);
		Dataset<Row> sentenceData = sparkSession.createDataFrame(rddrow, schema);
		
		Cleanser cleanser = new Cleanser().setInputColumn("description").setOutputColumn("cleaned_description");
		Tokenizer tokenizer = new Tokenizer().setInputCol("cleaned_description").setOutputCol("words");
		//Dataset<Row> wordsData = tokenizer.transform(sentenceData);

		StopWordsRemover stopWordsRemover = new StopWordsRemover().setInputCol("words")
				.setOutputCol("removed_stop_words").setCaseSensitive(false);
		//Dataset<Row> stopped_data = stopWordsRemover.transform(wordsData);

		Exploder exploder = new Exploder().setInputId("id").setInputColumn("removed_stop_words").setOutputColumn("singulars");
		Stemmer stemmer = new Stemmer().setInputId("id").setInputColumn("singulars").setOutputColumn("stemmed_word");
		Uniter uniter = new Uniter().setInputId("id").setInputColumn("stemmed_word").setOutputColumn("stemmed_word_list");
		
		HashingTF hashingTF = new HashingTF().setInputCol("stemmed_word_list").setOutputCol("rawFeatures").setNumFeatures(10000);

		//Dataset<Row> featurizedData = hashingTF.transform(stopped_data);
		// alternatively, CountVectorizer can also be used to get term frequency
		// vectors

		IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("final_features").setMinDocFreq(1);
		//Word2Vec w2v = new Word2Vec().setInputCol("stemmed_word_list").setOutputCol("new_features").setMinCount(1).setVectorSize(numFeatures);
		/*IDFModel idfModel = idf.fit(featurizedData);

		Dataset<Row> rescaledData = idfModel.transform(featurizedData);
		rescaledData.show();
*/
		//GaussianMixture gmm = new GaussianMixture().setFeaturesCol("new_features").setK(3);
		//GaussianMixtureModel model = gmm.fit(rescaledData);
		
		
		//BisectingKMeans bisectingKMeans = new BisectingKMeans().setFeaturesCol("new_features");
		Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{
				cleanser, 
				tokenizer, 
				stopWordsRemover, 
				exploder, 
				stemmer, 
				uniter, 
				hashingTF, 
				idf
		});
		PipelineModel pipelineModel = pipeline.fit(jdbcDF);
		pipelineModel.write().overwrite().save("target/pm");
		
		
		
		List<Row> jdbcDF_original = jdbcDF.collectAsList();
		jdbcDF = pipelineModel.transform(jdbcDF);
		double WSSSE_prec = Double.MAX_VALUE;
		int k = 0;
		//Map<Integer, Double> statistics = new HashMap<>();
		/*KMeansModel kmeansModel_prec = null;
		for (int i = 1 ; i<100 ; i++){
			KMeans kmeans = new KMeans().setK(i).setSeed(12345);
			KMeansModel kmeansModel = kmeans.run(jdbcDF.toJavaRDD().map(new Function<Row, Vector>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Vector call(Row inputRow) throws Exception {
					return Vectors.fromML(inputRow.getAs("new_features"));
				}
				
			}).rdd());
			double WSSSE = kmeansModel.computeCost(jdbcDF.toJavaRDD().map(new Function<Row, Vector>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Vector call(Row inputRow) throws Exception {
					return Vectors.fromML(inputRow.getAs("new_features"));
				}
				
			}).rdd());
			statistics.put(i, WSSSE);
			if (WSSSE > WSSSE_prec)
			{
				k = i - 1;
				System.out.println(k);
				break;
			}
			WSSSE_prec = WSSSE;
			kmeansModel_prec = kmeansModel;
			k = i;
		}
		System.out.println(k);
		System.out.println(statistics);
		JavaRDD<Integer> clusterIndexes = kmeansModel_prec.predict(jdbcDF.toJavaRDD().map(new Function<Row, Vector>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Vector call(Row inputRow) throws Exception {
				return Vectors.fromML(inputRow.getAs("new_features"));
			}
			
		}));*/
		KMeans kmeans = null;
		KMeansModel kmeansModel_prec = null;
		for (int i = 2 ; i<100 ; i++){
			kmeans = new KMeans().setK(i).setSeed(1).setFeaturesCol("final_features");
			KMeansModel kmeansModel = kmeans.fit(jdbcDF);
			kmeansModel.setFeaturesCol("final_features").computeCost(jdbcDF);
			double WSSSE = kmeansModel.setFeaturesCol("final_features").computeCost(jdbcDF);
			if (WSSSE > WSSSE_prec)
			{
				k = i - 1;
				System.out.println(k);
				break;
			}
			WSSSE_prec = WSSSE;
			kmeansModel_prec = kmeansModel;
			k = i;
		}
		
		kmeans.setK(21);
		kmeansModel_prec = kmeans.fit(jdbcDF);
		
		
		List<Row> jdbcDFList = jdbcDF.collectAsList();
		Map<Integer, List<Row>> map = new HashMap<>();
		for (int i = 0 ; i < jdbcDF_original.size(); i++){
			int cluster = kmeansModel_prec.predict(jdbcDFList.get(i).getAs("final_features"));
			if (map.containsKey(cluster))
				map.get(cluster).add(jdbcDF_original.get(i));
			else
			{
				map.put(cluster, new ArrayList<>());
				map.get(cluster).add(jdbcDF_original.get(i));
			}
		}
		map.forEach((clusterIndex, list) -> {
			list.forEach((row) -> {
				System.out.println("K-Means :: " + "Cluster " + clusterIndex + ":" + row.getAs("description"));
			});
		});
		
		
		/*System.out.println(k);
		System.out.println(statistics);
		
		List<Row> jdbcDFList = jdbcDF.collectAsList();
		Map<Integer, List<Row>> map = new HashMap<>();
		for (int i = 0 ; i < jdbcDF_original.size(); i++){
			int cluster = kmeansModel_prec.predict(jdbcDFList.get(i).getAs("new_features"));
			if (map.containsKey(cluster))
				map.get(cluster).add(jdbcDF_original.get(i));
			else
			{
				map.put(cluster, new ArrayList<>());
				map.get(cluster).add(jdbcDF_original.get(i));
			}
		}
		map.forEach((clusterIndex, list) -> {
			list.forEach((row) -> {
				System.out.println("K-Means :: " + "Cluster " + clusterIndex + ":" + row.getAs("description"));
			});
		});*/
		
		/*GaussianMixture gm = new GaussianMixture().setK(k).setFeaturesCol("new_features").setSeed(12345);
		GaussianMixtureModel gmm = gm.fit(jdbcDF).setFeaturesCol("new_features");
		
		for (int i = 0 ; i < jdbcDF_original.size(); i++){
			int cluster = gmm.predict(jdbcDFList.get(i).getAs("new_features"));
			if (map.containsKey(cluster))
				map.get(cluster).add(jdbcDF_original.get(i));
			else
			{
				map.put(cluster, new ArrayList<>());
				map.get(cluster).add(jdbcDF_original.get(i));
			}
		}
		map.forEach((clusterIndex, list) -> {
			list.forEach((row) -> {
				System.out.println("GMM :: " + "Cluster " + clusterIndex + ":" + row.getAs("description"));
			});
		});
		*/
		/*GaussianMixture gm = new GaussianMixture().setK(k-1);
		GaussianMixtureModel gmm = gm.run(jdbcDF.toJavaRDD().map(new Function<Row, Vector>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Vector call(Row inputRow) throws Exception {
				return Vectors.fromML(inputRow.getAs("new_features"));
			}
			
		}));
		for (int i = 0; i < gmm.k(); i++) {
			System.out.printf("Gaussian %d:\nweight=%f\nmu=%s\nsigma=\n%s\n\n", i, gmm.weights()[i],
					gmm.gaussians()[i].mu(), gmm.gaussians()[i].sigma());
		}*/
	}
}
