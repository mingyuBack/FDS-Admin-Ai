package kdt.project.fds.repository;

import kdt.project.fds.entity.TransactionFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TransactionFeatureRepository extends JpaRepository<TransactionFeature, Long> {
}