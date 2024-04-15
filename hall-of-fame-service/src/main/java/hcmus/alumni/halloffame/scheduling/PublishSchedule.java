package hcmus.alumni.halloffame.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import hcmus.alumni.halloffame.model.HallOfFameModel;
import hcmus.alumni.halloffame.model.StatusPostModel;
import hcmus.alumni.halloffame.repository.HallOfFameRepository;

@Component
public class PublishSchedule {
	@Value("${timeZone}")
	private String timeZone;
	@Autowired
	private HallOfFameRepository hofRepository;

	@Scheduled(cron = "0 0 * * * ?")
	public void publishNews() {
		try {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		Date now = cal.getTime();
		List<HallOfFameModel> list;
		
			list = hofRepository.getScheduledHof(now);
		
		for (HallOfFameModel n : list) {
			n.setStatus(new StatusPostModel(2));
			hofRepository.save(n);
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
