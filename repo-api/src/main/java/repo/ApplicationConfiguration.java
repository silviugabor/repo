package repo;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

import repo.spark.SparkSessionConfiguration;

//@SpringBootApplication(scanBasePackages = {"repo"})
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Import(SparkSessionConfiguration.class)
public class ApplicationConfiguration{

	public static void main (String[] args){
		SpringApplication.run(ApplicationConfiguration.class, args);
	}
	
}