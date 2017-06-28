package repo.spark.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.BisectingKMeans;
import org.apache.spark.ml.clustering.BisectingKMeansModel;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.Normalizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.linalg.BLAS;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import repo.crawler.dao.GitHubRepositoryDAO;
import repo.crawler.entities.github.GitHubRepository;
import repo.spark.comparator.GitHubRepositoryCosineSimilarityComparator;
import repo.spark.transformers.ByteArrayToString;
import repo.spark.transformers.Cleanser;
import repo.spark.transformers.Exploder;
import repo.spark.transformers.Stemmer;
import repo.spark.transformers.Uniter;

@Component
@PropertySource("classpath:spark.properties")
public class RepositoryMLService {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryMLService.class);
	private static final String JDBC_FORMAT = "jdbc";
	private static final String JDBC_URL = "url";
	private static final String JDBC_DBTABLE = "dbtable";
	private static final String JDBC_USER = "user";
	private static final String JDBC_PASSWORD = "password";
	
	private static final String REPOSITORY_ID_COLUMN_NAME = "id";
	private static final String REPOSITORY_DESCRIPTION_COLUMN_NAME = "description";

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

	@Autowired
	private MLService mlService;
	
	@Autowired
	private GitHubRepositoryDAO gitHubRepositoryDAO;
	
	private Dataset<Row> readJDBCTrainingSet() {
		Dataset<Row> jdbcTrainingSet = sparkSession.read()
				.format(JDBC_FORMAT)
				.option(JDBC_URL, jdbcURL)
				.option(JDBC_DBTABLE, jdbcDBTable)
				.option(JDBC_USER, jdbcUser)
				.option(JDBC_PASSWORD, jdbcPassword)
				.load();

//		jdbcTrainingSet = jdbcTrainingSet.select("id", "description");
		jdbcTrainingSet.count();
		jdbcTrainingSet.persist(StorageLevel.DISK_ONLY());
		
		return jdbcTrainingSet;
	}

	public Integer predictRepository(String idColumnName, String inputColumnName, Long repositoryId, String repositoryDescription, String modelDirectory) {
		Dataset<Row> predictionSet = sparkSession.createDataFrame(
				Arrays.asList(RowFactory.create(repositoryId, repositoryDescription.getBytes())),
				new StructType(new StructField[] {
						DataTypes.createStructField("id", DataTypes.LongType, false),
						DataTypes.createStructField("description", DataTypes.BinaryType, false)
						})
				);
		
		predictionSet.show();
		
		PipelineModel pipelineModel = mlService.loadPipelineModel(modelDirectory);
		predictionSet = pipelineModel.transform(predictionSet);
		predictionSet.show();
		
		Integer prediction = predictionSet.first().getAs("prediction");
		return prediction;
	}

	public Map<String, Object> clusterizeRepositories(String idColumnName, String inputColumnName, String modelOutputDirectory) {
		Dataset<Row> trainingSet = readJDBCTrainingSet();
		
		ByteArrayToString byteArrayToString = new ByteArrayToString().setInputId(idColumnName).setInputColumn(inputColumnName).setOutputColumn("string_description");
		Cleanser cleanser = new Cleanser().setInputColumn(byteArrayToString.getOutputColumn()).setOutputColumn("cleaned_description");
		Tokenizer tokenizer = new Tokenizer().setInputCol(cleanser.getOutputColumn()).setOutputCol("words");
		StopWordsRemover stopWordsRemover = new StopWordsRemover().setInputCol(tokenizer.getOutputCol()).setOutputCol("removed_stop_words").setCaseSensitive(false);
		Exploder exploder = new Exploder().setInputId(idColumnName).setInputColumn(stopWordsRemover.getOutputCol()).setOutputColumn("singulars");
		Stemmer stemmer = new Stemmer().setInputId(idColumnName).setInputColumn(exploder.getOutputColumn()).setOutputColumn("stemmed_word");
		Uniter uniter = new Uniter().setInputId(idColumnName).setInputColumn(stemmer.getOutputColumn()).setOutputColumn("stemmed_word_list");
		HashingTF hashingTF = new HashingTF().setInputCol(uniter.getOutputColumn()).setOutputCol("tf_features").setNumFeatures(8192);
		IDF idf = new IDF().setInputCol(hashingTF.getOutputCol()).setOutputCol("idf_features");
		Normalizer normalizer2 = new Normalizer().setP(2).setInputCol(idf.getOutputCol()).setOutputCol("final_scaled_features");

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
		
		List<Row> originalTrainingSetList = byteArrayToString.transform(trainingSet).collectAsList();
		Dataset<Row> transformedTrainingSet = pipelineModel.transform(trainingSet);
		List<Row> transformedTrainingSetList = transformedTrainingSet.collectAsList();
		
		BisectingKMeans bisectingKMeans = new BisectingKMeans();
		BisectingKMeansModel bisectingKMeansModel;
		bisectingKMeans.setK(70).setSeed(1L).setFeaturesCol(normalizer2.getOutputCol());
		bisectingKMeansModel = bisectingKMeans.fit(transformedTrainingSet);

		Map<Integer, List<Row>> map = new HashMap<>();
		for (int i = 0 ; i < transformedTrainingSetList.size(); i++){
			int cluster = bisectingKMeansModel.predict(transformedTrainingSetList.get(i).getAs("final_scaled_features"));
			GitHubRepository gitHubRepository = gitHubRepositoryDAO.getOne(transformedTrainingSetList.get(i).<Long>getAs("id"));
			gitHubRepository.setClusterNumber(cluster);
			gitHubRepositoryDAO.saveAndFlush(gitHubRepository);
			
			if (map.containsKey(cluster))
				map.get(cluster).add(originalTrainingSetList.get(i));
			else
			{
				map.put(cluster, new ArrayList<>());
				map.get(cluster).add(originalTrainingSetList.get(i));
			}
		}
		pipeline.setStages(new PipelineStage[]{
				byteArrayToString,
				cleanser, 
				tokenizer,
				stopWordsRemover,
				exploder, 
				stemmer, 
				uniter,
				hashingTF,
				idf,
				normalizer2,
				bisectingKMeans
		});
		PipelineModel previousPipelineModel = pipeline.fit(trainingSet);
	
		mlService.saveModel(previousPipelineModel, modelOutputDirectory);
		return getClusteringPipelineStatistics(previousPipelineModel, pipeline.getStages().length - 1, map);
	}
	
	private Map<String, Object> getClusteringPipelineStatistics(PipelineModel model, Integer clusteringAlgorithmStage, Map<Integer, List<Row>> clusterMap) {
		Map<String, Object> modelStatistics = getClusteringPipelineStatistics(model, clusteringAlgorithmStage);
		Map<String, Object> clusterStatistics = new LinkedHashMap<>();
		clusterMap.forEach((cluster, listOfRows)->clusterStatistics.put(String.valueOf(cluster), listOfRows.stream().<String>map((row)->String.format("Cluster %d : %s", cluster, new String(gitHubRepositoryDAO.findOne(row.<Long>getAs(0)).getDescription()))).collect(Collectors.toList())));
		modelStatistics.putAll(clusterStatistics);
		

		return modelStatistics;
	}

	private Map<String, Object> getClusteringPipelineStatistics(PipelineModel model, Integer clusteringAlgorithmStage) {
		Map<String, Object> modelStatistics = new LinkedHashMap<>();

		Vector[] centers = ((BisectingKMeansModel) model.stages()[clusteringAlgorithmStage]).clusterCenters();

		for (int i = 1; i <= centers.length; i++) {
			modelStatistics.put("Cluster " + i, "Number of non-zeros in vector: " + centers[i - 1].numNonzeros());
		}
		
//		printModelStatistics(modelStatistics);

		return modelStatistics;
	}
	
	public List<GitHubRepository> getRecommendationsFor(GitHubRepository gitHubRepository, int maximumNumberOfRecommendations, String modelDirectory){
		Assert.notNull(gitHubRepository, "Given GitHub repository must not be null!");
		//Assert.notNull(gitHubRepository.getRepositoryName(), "Given GitHub repository name must not be null!");
		Assert.notNull(gitHubRepository.getDescription(), "Given GitHub repository description must not be null!");
		//Assert.notNull(gitHubRepository.getClusterNumber(), "Given GitHub repository cluster number must not be null!");
		
		//get repositories from predicted cluster
		PipelineModel pipelineModel = mlService.loadPipelineModel(modelDirectory);
		Integer clusterNumber = predictRepository(REPOSITORY_ID_COLUMN_NAME, REPOSITORY_DESCRIPTION_COLUMN_NAME, gitHubRepository.getId(), new String(gitHubRepository.getDescription()), modelDirectory);
		List<GitHubRepository> intraClusterGitHubRepositories = gitHubRepositoryDAO.findByClusterNumber(clusterNumber);
		
		//compute normalized tf-idf features for each repository from predicted cluster using the pipeline model
		List<Row> rowList = new ArrayList<>();
		intraClusterGitHubRepositories.forEach((repo)->rowList.add(RowFactory.create(repo.getId(), repo.getDescription())));
		
		Dataset<Row> clusterDataset = sparkSession.createDataFrame(rowList, new StructType(new StructField[] {
				DataTypes.createStructField("id", DataTypes.LongType, false),
				DataTypes.createStructField("description", DataTypes.BinaryType, false)
				}));
		Dataset<Row> transformedClusterDataset = pipelineModel.transform(clusterDataset);
		List<Row> transformedClusterRepositoryList = transformedClusterDataset.collectAsList();
		
		//compute normalized tf-idf features for current given repository
		Dataset<Row> repositoryDataset = sparkSession.createDataFrame(
				Arrays.asList(RowFactory.create(gitHubRepository.getId(), gitHubRepository.getDescription())),
				new StructType(new StructField[] {
						DataTypes.createStructField("id", DataTypes.LongType, false),
						DataTypes.createStructField("description", DataTypes.BinaryType, false)
						})
		);
		Dataset<Row> transformedRepositoryDataset = pipelineModel.transform(repositoryDataset);
		Vector normalizedRepositoryTfIdfVector = transformedRepositoryDataset.collectAsList().get(0).getAs("final_scaled_features");
		
		//compute similarity score between the given repository and the cluster repositories
		for (int i = 0; i < transformedClusterRepositoryList.size() ; i++){
			Vector currentRepositoryNormalizedTfIdfVector = transformedClusterRepositoryList.get(i).getAs("final_scaled_features");
			intraClusterGitHubRepositories.get(i).setSimilarityScore(BLAS.dot(normalizedRepositoryTfIdfVector, currentRepositoryNormalizedTfIdfVector));
		}
		intraClusterGitHubRepositories.sort(new GitHubRepositoryCosineSimilarityComparator());
		
		if (maximumNumberOfRecommendations < intraClusterGitHubRepositories.size())
			return intraClusterGitHubRepositories.subList(0, maximumNumberOfRecommendations);
		else
			return intraClusterGitHubRepositories;
	}
	

	@SuppressWarnings("unused")
	private void printModelStatistics(Map<String, Object> modelStatistics) {
		LOG.info("\n------------------------------------------------");
		LOG.info("Model statistics:");
		LOG.info(modelStatistics.toString());
		LOG.info("------------------------------------------------\n");
	}

}
