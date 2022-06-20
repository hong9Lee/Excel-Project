package dev.excel.repository;

import dev.excel.dto.SampleVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataJpaRepository extends JpaRepository<SampleVO, Long> {

}
