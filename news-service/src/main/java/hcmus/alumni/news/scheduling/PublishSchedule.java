package hcmus.alumni.news.scheduling;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



import hcmus.alumni.news.model.NewsModel;
import hcmus.alumni.news.model.StatusPostModel;
import hcmus.alumni.news.repository.NewsRepository;

@Component
public class PublishSchedule {
	@Value("${timeZone}")
	private String timeZone;
	@Autowired
	private NewsRepository newsRepository;

	@Scheduled(cron = "0 0 * * * ?")
	public void publishNews() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		Date now = cal.getTime();
		List<NewsModel> list = newsRepository.getScheduledNews(now);
		for (NewsModel n : list) {
			n.setStatus(new StatusPostModel(2));
			newsRepository.save(n);
		}
	}
}