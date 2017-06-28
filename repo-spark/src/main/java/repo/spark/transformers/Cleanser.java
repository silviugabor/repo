package repo.spark.transformers;

import static org.apache.spark.sql.functions.column;
import static org.apache.spark.sql.functions.regexp_replace;
import static org.apache.spark.sql.functions.trim;

import java.io.IOException;
import java.util.UUID;

import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.util.DefaultParamsReader;
import org.apache.spark.ml.util.DefaultParamsWriter;
import org.apache.spark.ml.util.MLReader;
import org.apache.spark.ml.util.MLWritable;
import org.apache.spark.ml.util.MLWriter;
import org.apache.spark.ml.util.SchemaUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class Cleanser extends Transformer implements MLWritable {

	private static final long serialVersionUID = 1L;
	private String uid;

	private String inputColumn = "string_description";
	private String outputColumn = "cleaned_description";
	
    public Cleanser(String uid) {
        this.uid = uid;
    }

    public Cleanser() {
        this.uid = "Cleanser_" + UUID.randomUUID().toString();
    }

    @Override
    public Dataset<Row> transform(Dataset<?> sentences) {
    	// Remove all punctuation symbols.
        sentences = sentences.withColumn(outputColumn, regexp_replace(trim(column(inputColumn)), "([^\\w\\s])|(<[^>]+>)", ""));

        // Remove double spaces.
        return sentences.withColumn(outputColumn, regexp_replace(column(outputColumn), "\\s{2,}", " "));
    }

    @Override
    public StructType transformSchema(StructType schema) {
    	SchemaUtils.checkColumnType(schema, inputColumn, DataTypes.StringType, "Input type must be StringType!");
    	return schema.add(outputColumn, DataTypes.StringType, true);
    }

    @Override
    public Transformer copy(ParamMap extra) {
        return super.defaultCopy(extra);
    }

    @Override
    public String uid() {
        return this.uid;
    }

    @Override
    public MLWriter write() {
        return new DefaultParamsWriter(this);
    }

    @Override
    public void save(String path) throws IOException {
        write().save(path);
    }

    public static MLReader<Cleanser> read() {
        return new DefaultParamsReader<>();
    }

	public String getInputColumn() {
		return inputColumn;
	}

	public Cleanser setInputColumn(String inputColumn) {
		this.inputColumn = inputColumn;
		return this;
	}

	public String getOutputColumn() {
		return outputColumn;
	}

	public Cleanser setOutputColumn(String outputColumn) {
		this.outputColumn = outputColumn;
		return this;
	}

}