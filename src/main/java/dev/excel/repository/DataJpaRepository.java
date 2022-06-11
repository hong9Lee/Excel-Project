package dev.excel.repository;

import dev.excel.dto.ColumnsVO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataJpaRepository extends JpaRepository<ColumnsVO, Long> {

}
