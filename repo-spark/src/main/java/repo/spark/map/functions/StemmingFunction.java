package repo.spark.map.functions;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.tartarus.snowball.SnowballStemmer;

public class StemmingFunction implements MapFunction<Row, Row> {

	private static final long serialVersionUID = 1L;
	private SnowballStemmer englishStemmer;
	
	private String inputId;
	private String inputColumn;
	private String outputColumn;
	
	public StemmingFunction() {
		this.englishStemmer = initializeEnglishStemmer();
	}

	private SnowballStemmer initializeEnglishStemmer() {
		try {
			Class<?> stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
			return (SnowballStemmer) stemClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public Row call(Row input) throws Exception {
		englishStemmer.setCurrent(input.getAs(inputColumn));
		englishStemmer.stem();
		String stemmedWord = englishStemmer.getCurrent();
		return RowFactory.create(input.getLong(input.schema().fieldIndex(inputId)), stemmedWord);
	}

	public String getOutputColumn() {
		return outputColumn;
	}

	public StemmingFunction setOutputColumn(String outputColumn) {
		this.outputColumn = outputColumn;
		return this;
	}

	public String getInputColumn() {
		return inputColumn;
	}

	public StemmingFunction setInputColumn(String inputColumn) {
		this.inputColumn = inputColumn;
		return this;
	}

	public String getInputId() {
		return inputId;
	}

	public StemmingFunction setInputId(String inputId) {
		this.inputId = inputId;
		return this;
	}

}