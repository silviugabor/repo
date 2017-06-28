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
import org.apache.spark.ml.util.SchemaUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import static org.apache.spark.sql.functions.*;

public class Exploder extends Transformer implements MLWritable {

	private static final long serialVersionUID = 1L;
	private String uid;

	private String inputId = "id";
	private String inputColumn = "removed_stop_words";
	private String outputColumn = "singulars";
	
    public Exploder(String uid) {
        this.uid = uid;
    }

    public Exploder() {
        this.uid = "Exploder_" + UUID.randomUUID().toString();
    }

    @Override
    public Dataset<Row> transform(Dataset<?> sentences) {
        // Create as many rows as elements in inputColumn column (extract nested data)
        Column singular = explode(column(inputColumn)).as(outputColumn);
        return sentences.select(column(inputId), column(inputColumn), singular);
    }

    @Override
    public StructType transformSchema(StructType schema) {
    	SchemaUtils.checkColumnType(schema, inputColumn, DataTypes.createArrayType(DataTypes.StringType), "Input type must be ArrayType(StringType) but got $inputType.");
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

    public static MLReader<Exploder> read() {
        return new DefaultParamsReader<>();
    }

	public String getInputColumn() {
		return inputColumn;
	}

	public Exploder setInputColumn(String inputColumn) {
		this.inputColumn = inputColumn;
		return this;
	}

	public String getOutputColumn() {
		return outputColumn;
	}

	public Exploder setOutputColumn(String outputColumn) {
		this.outputColumn = outputColumn;
		return this;
	}

	public String getInputId() {
		return inputId;
	}

	public Exploder setInputId(String inputId) {
		this.inputId = inputId;
		return this;
	}
}
