package com.example.ordersystem.ordering.repository;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.ordering.domain.Ordering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    Page<Ordering> findByMember(Member member, Pageable pageable);
}
