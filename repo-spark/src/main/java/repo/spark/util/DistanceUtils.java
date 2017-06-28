package repo.spark.util;

import org.apache.spark.ml.linalg.BLAS;
import org.apache.spark.ml.linalg.Vector;

public final class DistanceUtils {

	private DistanceUtils() {};

	public static double getCosineSimilarityForL2NormalizedVectors(Vector normalizedVector1, Vector normalizedVector2) {
		return BLAS.dot(normalizedVector1, normalizedVector2);
	}

}
