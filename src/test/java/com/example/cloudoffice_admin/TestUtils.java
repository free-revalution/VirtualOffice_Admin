package com.example.cloudoffice_admin;

import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.model.VirtualSpace;
import com.example.cloudoffice_admin.model.Zone;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_PASSWORD = "Test@123";
    public static final String TEST_USERNAME = "testuser";
    
    // 生成测试用户
    public static User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail(TEST_EMAIL);
        user.setPassword(new BCryptPasswordEncoder().encode(TEST_PASSWORD));
        user.setUsername(TEST_USERNAME + id);
        user.setFullName("Test User" + id);
        user.setRole("USER");
        user.setStatus("online"); // 使用online状态而不是ACTIVE
        user.setLastActive(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
    
    // 生成测试虚拟空间
    public static VirtualSpace createTestSpace(Long id, Long creatorId) {
        VirtualSpace space = new VirtualSpace();
        space.setId(id);
        space.setName("Test Space " + id);
        space.setDescription("Test Description for Space " + id);
        space.setCreatorId(creatorId);
        space.setPublic(false);
        space.setCreatedAt(LocalDateTime.now());
        return space;
    }
    
    // 生成测试区域
    public static Zone createTestZone(Long id, Long spaceId) {
        Zone zone = new Zone();
        zone.setId(id);
        zone.setSpaceId(spaceId);
        zone.setName("Test Zone " + id);
        zone.setDescription("Test Description for Zone " + id);
        zone.setType("workspace"); // 添加type字段设置
        zone.setColor("#4CAF50"); // 添加color字段设置
        zone.setPrivate(false); // 使用setPrivate方法设置是否私有
        zone.setX(100);
        zone.setY(100);
        zone.setWidth(200);
        zone.setHeight(150);
        return zone;
    }
    
    // 生成多个测试用户
    public static List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(createTestUser((long) i));
        }
        return users;
    }
    
    // 生成多个测试空间
    public static List<VirtualSpace> createTestSpaces(int count, Long creatorId) {
        List<VirtualSpace> spaces = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            spaces.add(createTestSpace((long) i, creatorId));
        }
        return spaces;
    }
    
    // 生成多个测试区域
    public static List<Zone> createTestZones(int count, Long spaceId) {
        List<Zone> zones = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            zones.add(createTestZone((long) i, spaceId));
        }
        return zones;
    }
}
