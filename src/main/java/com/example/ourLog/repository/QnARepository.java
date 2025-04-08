package com.example.ourLog.repository;

import com.example.ourLog.entity.QnA;
import com.example.ourLog.repository.search.SearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnARepository extends JpaRepository<QnA, Long>, SearchRepository {
}
