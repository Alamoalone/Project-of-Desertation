package com.todomypet.todoservice.domain.node;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Node("Todo")
@Builder
@Getter
public class Todo {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;
    @Property("content")
    private String content;
    @Property("startedAtDate")
    private LocalDate startedAtDate;
    @Property("startedAtTime")
    private LocalTime startedAtTime;
    @Property("endedAtDate")
    private LocalDate endedAtDate;
    @Property("endedAtTime")
    private LocalTime endedAtTime;
    @Property("alertAt")
    private LocalDateTime alertAt;
    @Property("receiveAlert")
    private boolean receiveAlert;
    @Property("alertType")
    private AlertType alertType;
    @Property("clearYN")
    private boolean clearYN;
    @Property("getExperiencePointOrNot")
    private boolean getExperiencePointOrNot;
    @Property("markOnTheCalenderOrNot")
    private boolean markOnTheCalenderOrNot;
    @Property("repeatType")
    private RepeatType repeatType;
    @Property("repeatData")
    private List<Integer> repeatData;
    @Property("repeatCode")
    private String repeatCode;
}
