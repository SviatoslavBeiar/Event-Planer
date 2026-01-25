package socialMediaApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import socialMediaApp.models.OrganizerRequest;
import socialMediaApp.models.enums.OrganizerRequestStatus;
import socialMediaApp.responses.admin.OrganizerRequestAdminDto;

import java.util.List;
import java.util.Optional;

public interface OrganizerRequestRepository extends JpaRepository<OrganizerRequest, Long> {
    boolean existsByUser_Id(int userId);
    Optional<OrganizerRequest> findByUser_Id(int userId);

    @Query("""
        select new socialMediaApp.responses.admin.OrganizerRequestAdminDto(
            r.id, r.status, r.createdAt, r.reviewedAt, r.note,
            u.id, u.email, u.name, u.lastName
        )
        from OrganizerRequest r
        join r.user u
        where r.status = :status
        order by r.createdAt desc
    """)
    List<OrganizerRequestAdminDto> findDtosByStatus(@Param("status") OrganizerRequestStatus status);

}
