package sn.sunufarmasi.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.user.entity.User;
import sn.sunufarmasi.user.entity.User.RoleUser;
import sn.sunufarmasi.user.entity.User.StatutUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByTelephone(String telephone);

    Optional<User> findByEmailOrTelephone(String email, String telephone);

    boolean existsByEmail(String email);

    boolean existsByTelephone(String telephone);

    List<User> findByRole(RoleUser role);

    List<User> findByStatut(StatutUser statut);

    List<User> findByRoleAndStatut(RoleUser role, StatutUser statut);

    Page<User> findByRole(RoleUser role, Pageable pageable);

    long countByRole(RoleUser role);

    long countByStatut(StatutUser statut);

    @Query("""
        SELECT u FROM User u 
        WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
           OR u.telephone LIKE CONCAT('%', :search, '%')
        ORDER BY u.nom, u.prenom
    """)
    List<User> search(@Param("search") String search);
}
