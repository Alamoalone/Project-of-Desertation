package com.todomypet.todoservice.repository;

import com.todomypet.todoservice.domain.node.Todo;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@EnableNeo4jRepositories
public interface TodoRepository extends Neo4jRepository<Todo, String> {
    @Query("MATCH (t:Todo{id:$todoId}) RETURN t.getExperiencePointOrNot")
    boolean isGetExperiencePoint(String todoId);

    @Query("MATCH (t:Todo{id:$todoId}) SET t.clearYN = true, t.getExperiencePointOrNot = true")
    void updateClearYNAndGetExperienceByTodoId(String todoId);

    @Query("MATCH (u:User{id:$userId}) WITH u " +
            "MATCH (u)-[:HAVE]-(c:Category{id:$categoryId})-[i:INCLUDE]->(t:Todo) " +
            "return t")
    List<Todo> getAllTodoByCategoryId(String userId, String categoryId);

    @Query("MATCH (t:Todo{id:$todoId}) SET t.clearYN = false")
    void updateClearYNToUnclearByTodoId(String todoId);

    @Query("MATCH (t:Todo{id:$todoId}) DETACH DELETE t")
    void deleteTodoByTodoId(String todoId);

    @Query("MATCH (u:User{id:$userId}) WITH u " +
            "MATCH (u)-[:HAVE]->(c:Category)-[:INCLUDE]->(t:Todo{id:$todoId}) " +
            "RETURN t")
    Optional<Todo> existsByUserIdAndTodoId(String userId, String todoId);

    @Query("MATCH (u:User {id:$userId})-[:HAVE]->(c:Category)-[:INCLUDE]->(t:Todo) " +
            "WHERE date(t.startedAtDate).year = $year AND date(t.startedAtDate).month = $month OR " +
            "date(t.endedAtDate).year = $year AND date(t.startedAtDate).month = $month " +
            "OR (date(t.startedAtDate) <= date({year:$year, month:$month}) <= date(t.endedAtDate)) " +
            "RETURN DISTINCT t ORDER BY t.id ASC")
    List<Todo> getAllTodoByUserAndMonth(String userId, int year, int month);

    @Query("MATCH (t:Todo{id:$repeatCode}) RETURN t")
    List<Todo> getTodoByRepeatCode(String repeatCode);

    @Query("MATCH (t:Todo{id:%todoId}) DETACH DELETE todoId")
    void deleteOneById(String todoId);

    @Query("MATCH (t:Todo{id:$todoId}) RETURN t")
    Optional<Todo> getOneTodoByTodoId(String todoId);

    @Query("MATCH (t:Todo{repeatCode:$repeatCode}) DETACH DELETE t")
    void deleteAllByRepeatCode(String repeatCode);

    @Query("MATCH (t:Todo{repeatCode:$repeatCode}) DETACH DELETE t")
    void endTheRepeatTodoByRepeatCode(String repeatCode);
}
