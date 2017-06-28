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
import static org.apache.spark.sql.functions.*;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class Uniter extends Transformer implements MLWritable {

	private static final long serialVersionUID = 1L;
	private String uid;

	private String inputId = "id";
	private String inputColumn = "stemmed_word";
	private String outputColumn = "stemmed_word_list";
    public Uniter(String uid) {
        this.uid = uid;
    }

    public Uniter() {
        this.uid = "Uniter_" + UUID.randomUUID().toString();
    }

    @Override
    public Dataset<Row> transform(Dataset<?> words) {
        // Unite words into a sentence again.
        Dataset<Row> stemmedSentences = words.groupBy(column(inputId)).agg(collect_list(inputColumn).as(outputColumn)).sort(column(inputId), column(outputColumn));
        
        return stemmedSentences;
    }

    @Override
    public StructType transformSchema(StructType schema) {
        return schema.add(outputColumn, DataTypes.createArrayType(DataTypes.StringType), false);
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

    public static MLReader<Uniter> read() {
        return new DefaultParamsReader<>();
    }

	public String getInputId() {
		return inputId;
	}

	public Uniter setInputId(String inputId) {
		this.inputId = inputId;
		return this;
	}

	public String getInputColumn() {
		return inputColumn;
	}

	public Uniter setInputColumn(String inputColumn) {
		this.inputColumn = inputColumn;
		return this;
	}

	public String getOutputColumn() {
		return outputColumn;
	}

	public Uniter setOutputColumn(String outputColumn) {
		this.outputColumn = outputColumn;
		return this;
	}

}