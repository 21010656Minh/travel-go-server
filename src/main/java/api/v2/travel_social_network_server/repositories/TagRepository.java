package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    Optional<Tag> findBySlug(String slug);
    
    Optional<Tag> findByTitle(String title);
    
    @Query("""
        SELECT t FROM Tag t 
        WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY t.createdAt DESC
        LIMIT :limit
        """)
    List<Tag> searchByTitle(@Param("query") String query, @Param("limit") int limit);
    
    @Query("""
        SELECT t FROM Tag t
        ORDER BY t.createdAt DESC
        LIMIT :limit
        """)
    List<Tag> findRecentTags(@Param("limit") int limit);
}
