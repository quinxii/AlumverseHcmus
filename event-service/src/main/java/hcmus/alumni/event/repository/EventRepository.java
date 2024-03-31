package hcmus.alumni.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.EventModel;

public interface EventRepository  extends JpaRepository<EventModel, String> {
	
}
