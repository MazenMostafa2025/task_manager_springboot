package com.mazen.wfm.dtos.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class Wrappers {

    public static class ResponseWrapperProject extends ResponseWrapper<ProjectResponse> {
        @Schema(description = "payload data", implementation = ProjectResponse.class)
        private ProjectResponse data;
    }
//    public static class UpdateProjectFail extends ResponseWrapper<ProjectResponse> {
//        @Schema(description = "payload data", implementation = ProjectResponse.class)
//        private ProjectResponse data;
//    }
    public static class ResponseWrapperProjectList extends ResponseWrapper<List<ProjectResponse>> {
        @ArraySchema(schema = @Schema(implementation = ProjectResponse.class),
                arraySchema = @Schema(description = "payload data"))
        private List<ProjectResponse> data;
    }
    public static class ResponseWrapperComment extends ResponseWrapper<CommentResponse> {
        @Schema(description = "payload data", implementation = CommentResponse.class)
        private CommentResponse data;
    }
    public static class ResponseWrapperCommentList extends ResponseWrapper<List<CommentResponse>> {
        @ArraySchema(schema = @Schema(implementation = CommentResponse.class),
                arraySchema = @Schema(description = "payload data"))
        private List<CommentResponse> data;
    }
    public static class ResponseWrapperTask extends ResponseWrapper<TaskResponse> {
        @Schema(description = "payload data", implementation = TaskResponse.class)
        private TaskResponse data;
    }
    public static class ResponseWrapperTaskList extends ResponseWrapper<List<TaskResponse>> {
        @ArraySchema(schema = @Schema(implementation = TaskResponse.class),
                arraySchema = @Schema(description = "payload data"))
        private List<TaskResponse> data;
    }

}
