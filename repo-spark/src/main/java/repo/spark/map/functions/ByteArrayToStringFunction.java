package repo.spark.map.functions;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

public class ByteArrayToStringFunction implements MapFunction<Row, Row>{

	private static final long serialVersionUID = 1L;

	private String inputId;
	private String inputColumn;
	
	public ByteArrayToStringFunction() {
		this.inputId = "id";
		this.inputColumn = "description";
	}
	public ByteArrayToStringFunction(String inputId, String inputColumn) {
		this.inputId = inputId;
		this.inputColumn = inputColumn;
	}
	
	@Override
	public Row call(Row inputRow) throws Exception {
		byte[] inputColumnByteArray = inputRow.<byte[]>getAs(inputColumn);
		String outputColumnString = (String) Class.forName("java.lang.String").getConstructor(byte[].class).newInstance(inputColumnByteArray);
		return RowFactory.create(inputRow.getLong(inputRow.schema().fieldIndex(inputId)), outputColumnString);
	}
	public String getInputId() {
		return inputId;
	}
	public void setInputId(String inputId) {
		this.inputId = inputId;
	}
	public String getInputColumn() {
		return inputColumn;
	}
	public void setInputColumn(String inputColumn) {
		this.inputColumn = inputColumn;
	}

}
