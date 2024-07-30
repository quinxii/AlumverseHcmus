package hcmus.alumni.counsel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CounselServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CounselServiceApplication.class, args);
	}

}
