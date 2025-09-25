package com.mazen.wfm.mapper;

import com.mazen.wfm.dtos.response.CommentResponse;
import com.mazen.wfm.models.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "taskId", source = "task.taskId")
    @Mapping(target = "authorId", source = "author.userId")
    @Mapping(target = "authorName", source = "author.username")
    CommentResponse toResponse(Comment comment);
}
