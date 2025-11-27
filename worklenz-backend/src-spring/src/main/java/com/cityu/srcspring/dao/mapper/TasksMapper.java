package com.cityu.srcspring.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cityu.srcspring.model.entity.Tasks;
import com.cityu.srcspring.model.vo.TaskVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TasksMapper extends BaseMapper<Tasks> {
    // 查询 status name
    @Select("SELECT name FROM task_statuses WHERE id = #{id}")
    String selectStatusNameById(UUID id);

    // 查询 priority name
    @Select("SELECT name FROM task_priorities WHERE id = #{id}")
    String selectPriorityNameById(UUID id);

    // 查询 parent task name
    @Select("SELECT name FROM tasks WHERE id = #{id}")
    String selectTaskNameById(UUID id);

    @Select("SELECT name FROM users WHERE id = #{id}")
    String selecUserNameById(UUID id);

    @Select("SELECT name FROM projects WHERE id = #{id}")
    String selectProjectNameById(UUID id);

    // 根据 sprint_id 查询任务
    @Select("SELECT * FROM tasks WHERE sprint_id = #{sprintId}")
    List<Tasks> selectTasksBySprintId(@Param("sprintId") Integer sprintId);

  @Select("""
        SELECT
            t.*,
            ts.name AS status,
            tp.name AS priorityName,
            pt.name AS parentTaskName,
            u.name AS reporterName,
            p.name AS projectName
        FROM tasks t
        LEFT JOIN task_statuses ts ON t.status_id = ts.id
        LEFT JOIN task_priorities tp ON t.priority_id = tp.id
        LEFT JOIN tasks pt ON t.parent_task_id = pt.id
        LEFT JOIN users u ON t.reporter_id = u.id
        LEFT JOIN projects p ON t.project_id = p.id
        WHERE t.sprint_id = #{sprintId}
    """)
  List<TaskVO> selectTaskVOBySprintId(@Param("sprintId") Integer sprintId);
}


