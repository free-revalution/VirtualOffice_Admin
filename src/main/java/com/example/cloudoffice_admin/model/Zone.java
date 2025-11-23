package com.example.cloudoffice_admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "zones")
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zoneId;

    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private VirtualSpace space;
    
    // 用于直接存储空间ID的字段，避免在某些场景下需要懒加载VirtualSpace对象
    @Column(name = "space_id", insertable = false, updatable = false)
    private Long spaceId;

    @Column(nullable = false)
    private String name;
    
    // 区域描述
    private String description;
    
    // 区域颜色
    private String color;
    
    // 是否为私有区域
    private boolean isPrivate = false;

    private String type; // workspace, meeting, etc.

    // 坐标和尺寸
    private int x;
    private int y;
    private int width;
    private int height;
    
    // 额外的setter方法，用于兼容VirtualSpaceService中的调用
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
