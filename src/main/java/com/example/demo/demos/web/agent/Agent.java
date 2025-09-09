package com.example.demo.demos.web.agent;

import javax.persistence.*;

@Entity
@Table(name = "agents")
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "total_processes", nullable = false)
    private Integer totalProcesses;

    @Column(name = "available_processes", nullable = false)
    private Integer availableProcesses;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 100)
    private String location;

    // ===== getter/setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Integer getTotalProcesses() { return totalProcesses; }
    public void setTotalProcesses(Integer totalProcesses) { this.totalProcesses = totalProcesses; }

    public Integer getAvailableProcesses() { return availableProcesses; }
    public void setAvailableProcesses(Integer availableProcesses) { this.availableProcesses = availableProcesses; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}