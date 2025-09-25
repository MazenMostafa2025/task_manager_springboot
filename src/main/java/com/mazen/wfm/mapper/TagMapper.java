package com.mazen.wfm.mapper;

import com.mazen.wfm.dtos.TagDTO;
import com.mazen.wfm.models.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagDTO toDto(Tag tag);
}
