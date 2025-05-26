package com.example.ourLog.repository;

import com.example.ourLog.entity.Favorite;
import com.example.ourLog.entity.Post;
import com.example.ourLog.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.ourLog.entity.QPost.post;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FavoriteInsertTest {

}


