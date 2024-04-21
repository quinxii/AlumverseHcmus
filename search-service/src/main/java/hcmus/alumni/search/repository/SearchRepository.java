package hcmus.alumni.search.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.search.dto.ISearchDto;
import hcmus.alumni.search.model.HallOfFameModel;
import hcmus.alumni.search.model.UserModel;

public interface SearchRepository extends JpaRepository<UserModel, String> {
	@Query("SELECT u FROM UserModel u WHERE " + "u.fullName LIKE %:input% OR " + "u.email LIKE %:input% OR "
			+ "u.socialMediaLink LIKE %:input% OR " + "u.aboutMe LIKE %:input% OR " + "u.phone LIKE %:input%")
	List<UserModel> searchUsers(@Param("input") String input);

}