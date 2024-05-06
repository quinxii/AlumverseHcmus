package hcmus.alumni.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.search.dto.ISearchDto;
import hcmus.alumni.search.model.UserModel;

public interface SearchRepository extends JpaRepository<UserModel, String> {

	@Query("SELECT DISTINCT u " + 
		       "FROM UserModel u " + 
		       "LEFT JOIN u.status s " + 
		       "LEFT JOIN u.faculty f " + 
		       "WHERE u.fullName LIKE %:fullName% " +
		       "AND ((:facultyName IS NULL OR :facultyName = '') OR LOWER(f.name) LIKE %:facultyName%) " + 
		       "AND ((:statusName IS NULL OR :statusName = '') OR LOWER(s.name) LIKE %:statusName%)")
		Page<ISearchDto> searchUsers(String fullName, String statusName, String facultyName, Pageable pageable);

	@Query("SELECT COUNT(u) FROM UserModel u JOIN u.status s WHERE s.name = :statusName")
	Long getCountByStatus(@Param("statusName") String statusName);

}