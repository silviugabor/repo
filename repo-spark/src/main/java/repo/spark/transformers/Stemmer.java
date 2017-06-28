package repo.spark.transformers;

import java.io.IOException;
import java.util.UUID;

import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.util.DefaultParamsReader;
import org.apache.spark.ml.util.DefaultParamsWriter;
import org.apache.spark.ml.util.MLReader;
import org.apache.spark.ml.util.MLWritable;
import org.apache.spark.ml.util.MLWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import repo.spark.map.functions.StemmingFunction;


public class Stemmer extends Transformer implements MLWritable {

	private static final long serialVersionUID = 1L;
	private String uid;
	
	private String inputId = "id";
	private String inputColumn = "singulars";
	private String outputColumn = "stemmed_word";

    public Stemmer(String uid) {
        this.uid = uid;
    }

    public Stemmer() {
        this.uid = "Stemmer" + "_" + UUID.randomUUID().toString();
    }

    @Override
    public String uid() {
        return this.uid;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public Dataset<Row> transform(Dataset dataset) {
        return dataset.map(new StemmingFunction().setInputId(inputId).setInputColumn(inputColumn).setOutputColumn(outputColumn), RowEncoder.apply(transformSchema(dataset.schema())));
    }

    @Override
    public StructType transformSchema(StructType schema) {
        return new StructType(new StructField[]{
        		new StructField(inputId, DataTypes.LongType, false, Metadata.empty()),
        		new StructField(outputColumn, DataTypes.StringType, false, Metadata.empty())
        });
    }

    @Override
    public Transformer copy(ParamMap extra) {
        return super.defaultCopy(extra);
    }

    @Override
    public MLWriter write() {
        return new DefaultParamsWriter(this);
    }

    @Override
    public void save(String path) throws IOException {
        write().save(path);
    }

    public static MLReader<Stemmer> read() {
        return new DefaultParamsReader<>();
    }

	public String getInputColumn() {
		return inputColumn;
	}

	public Stemmer setInputColumn(String inputColumn) {
		this.inputColumn = inputColumn;
		return this;
	}

	public String getOutputColumn() {
		return outputColumn;
	}

	public Stemmer setOutputColumn(String outputColumn) {
		this.outputColumn = outputColumn;
		return this;
	}

	public String getInputId() {
		return inputId;
	}

	public Stemmer setInputId(String inputId) {
		this.inputId = inputId;
		return this;
	}

}