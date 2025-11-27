package com.cityu.srcspring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cityu.srcspring.dao.mapper.TasksMapper;
import com.cityu.srcspring.model.dto.SprintDTO;
import com.cityu.srcspring.model.entity.Projects;
import com.cityu.srcspring.model.entity.Sprints;
import com.cityu.srcspring.dao.mapper.ProjectsMapper;
import com.cityu.srcspring.dao.mapper.SprintsMapper;
import com.cityu.srcspring.model.entity.Tasks;
import com.cityu.srcspring.model.vo.TaskVO;
import com.cityu.srcspring.service.SprintsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.ibatis.type.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SprintsServiceImpl implements SprintsService {
    @Autowired
    private SprintsMapper sprintsMapper;
    @Autowired
    private ProjectsMapper projectsMapper;
    @Autowired
    private TasksMapper taskMapper;



  @Transactional
  @Override
  public boolean delete(Integer id) {
    // 1️⃣ 先把所有属于该 sprint 的任务的 sprint_id 置为空
    UpdateWrapper<Tasks> updateWrapper = new UpdateWrapper<>();
    updateWrapper.eq("sprint_id", id)
      .set("sprint_id", null);
    taskMapper.update(null, updateWrapper);

    // 2️⃣ 再删除 sprint 本身
    return sprintsMapper.deleteById(id) > 0;
  }


  @Override
    public boolean add(Sprints sprints) {
        return sprintsMapper.insert(sprints) > 0;
    }

  private final ObjectMapper objectMapper = new ObjectMapper();



  @Override
  public Object page(int page, int size) {
    // 1️⃣ 分页查询 Sprint
    Page<Sprints> sprintPage = sprintsMapper.selectPage(new Page<>(page, size), null);

    // 2️⃣ 遍历每个 Sprint，构建 DTO
    List<SprintDTO> result = sprintPage.getRecords().stream().map(sprint -> {
      SprintDTO dto = new SprintDTO();
      BeanUtils.copyProperties(sprint, dto);

      // 3️⃣ 直接通过 Mapper 查询任务列表
      List<TaskVO> tasks = taskMapper.selectTaskVOBySprintId(sprint.getId());
      dto.setSubtask(tasks != null ? tasks : Collections.emptyList());

      // 4️⃣ 关联 project 名称
      if (dto.getProjectId() != null) {
        Projects project = projectsMapper.selectById(dto.getProjectId());
        dto.setProjectName(project != null ? project.getName() : null);
      }

      return dto;
    }).collect(Collectors.toList());

    // 5️⃣ 构建分页结果
    Map<String, Object> pageResult = new HashMap<>();
    pageResult.put("total", sprintPage.getTotal());
    pageResult.put("records", result);

    return pageResult;
  }


    @Override
    public boolean update(Sprints sprints) {
        return sprintsMapper.updateById(sprints) > 0;
    }

    @Override
    public List<SprintDTO> getByProjectId(UUID projectId) {
        List<Sprints> sprints = sprintsMapper.selectList(new QueryWrapper<Sprints>().eq("project_id", projectId));
        Projects project = projectsMapper.selectById(projectId);

        return sprints.stream().map(sprint -> {
            SprintDTO dto = new SprintDTO();
            BeanUtils.copyProperties(sprint, dto);
            dto.setProjectName(project != null ? project.getName() : null);
            return dto;
        }).collect(Collectors.toList());
    }

  @Override
  public Sprints get1(Integer id) {
    return sprintsMapper.selectById(id);
  }


  @Override
  public SprintDTO get(Integer id) {
    Sprints sprint = sprintsMapper.selectById(id);
    if (sprint == null) return null;

    SprintDTO dto = new SprintDTO();
    BeanUtils.copyProperties(sprint, dto);

    // 🧩 直接通过 Mapper 查询任务及关联字段
    List<TaskVO> tasks = taskMapper.selectTaskVOBySprintId(sprint.getId());
    dto.setSubtask(tasks);

    // 🏗️ 关联 project 名称（如果没有在 Mapper 查询）
    if (dto.getProjectId() != null && dto.getProjectName() == null) {
      dto.setProjectName(taskMapper.selectProjectNameById(dto.getProjectId()));
    }

    return dto;
  }








}
